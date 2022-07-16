package de.budschie.bmorph.json_integration;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry.AbilityGroup;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/** https://forums.minecraftforge.net/topic/100915-1165-make-mod-data-editable-with-datapack-adding-new-data-type/ **/
public class MorphAbilityManager extends SimpleJsonResourceReloadListener
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).create();
	private HashMap<EntityType<?>, List<Ability>> entityAbilityLookup = new HashMap<>();
	
	private HashMap<ResourceLocation, List<Ability>> customAbilityLookup = new HashMap<>();
	
	private Runnable lazyResolveEntity;
	private Runnable lazyResolveCustom;
	
	public MorphAbilityManager()
	{
		super(GSON, "morph_abilities");
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public List<Ability> getAbilitiesForEntity(EntityType<?> entity)
	{
		if(lazyResolveEntity != null)
		{
			lazyResolveEntity.run();
			lazyResolveEntity = null;
		}
		
		return entityAbilityLookup.get(entity);
	}
	
	@Nullable
	@Deprecated(forRemoval = true)
	/** This method will return null if there is no ability for the given entity type.
	 * @deprecated Use {@link MorphItem#getAbilities()} instead. This is the method this method calls now. **/
	public List<Ability> getAbilitiesForItem(MorphItem morphItem)
	{
		return morphItem.getAbilities();
	}
	
	@Nullable
	/** This method will return null if there is no list of abilities for the given custom ability list key. **/
	public List<Ability> getAbilitiesForCustomAbilityList(ResourceLocation customAbilityListName)
	{
		if(lazyResolveCustom != null)
		{
			lazyResolveCustom.run();
			lazyResolveCustom = null;
		}
		
		return customAbilityLookup.get(customAbilityListName);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn,
			ProfilerFiller profilerIn)
	{
		entityAbilityLookup.clear();
		
		HashMap<MorphAbilityKey, MorphAbilityEntry> abilityEntries = new HashMap<>();
		HashMap<ResourceLocation, MorphAbilityEntry> abilityEntityTagEntries = new HashMap<>();
		
		// Load in all granted and revoked abilities and store this data in an HashMap with a String representing the entity resource location as a 
		// key and a MorphAbilityEntry as a value.
		objectIn.forEach((resourceLocation, json) ->
		{
			try
			{
				JsonObject root = json.getAsJsonObject();
				
				MorphAbilityKey key = null;
				// I should probably refactor this code in the future...
				boolean isEntityTag = false;
				ResourceLocation entityTag = null;
				
				// Dumb spaghetti code but idc
				if(root.has("entity_type"))
				{
					String entityType = root.get("entity_type").getAsString();
					
					if(isAbilityGroup(entityType))
					{
						isEntityTag = true;
						entityTag = new ResourceLocation(stripAbilityGroupIndicator(entityType));
					}
					else
					{
						key = new MorphAbilityKey(new ResourceLocation(entityType), MorphAbilityEntryType.ENTITY);
					}
				}
				else if(root.has("ability_list_name"))
				{
					key = new MorphAbilityKey(new ResourceLocation(root.get("ability_list_name").getAsString()), MorphAbilityEntryType.CUSTOM);
				}
				else
				{
					LOGGER.warn("There was neither an \"entity_type\" nor an \"ability_list_name\" present in " + resourceLocation.toString() + ".");
				}
				
				JsonArray grantedAbilities = root.getAsJsonArray("grant");
				JsonArray revokedAbilities = root.getAsJsonArray("revoke");

				// Sorry for the naming lul
				MorphAbilityEntry entry = null;
				
				if(isEntityTag)
				{
					entry = abilityEntityTagEntries.computeIfAbsent(entityTag, keyLambda -> new MorphAbilityEntry());
				}
				else
				{
					entry = abilityEntries.computeIfAbsent(key, keyLambda -> new MorphAbilityEntry());
				}

				for (int i = 0; i < grantedAbilities.size(); i++)
				{
					String grantedString = grantedAbilities.get(i).getAsString();
					if(isAbilityGroup(grantedString))
						entry.grantAbilityGroup(stripAbilityGroupIndicator(grantedString));
					else
						entry.grantAbility(grantedString);
				}

				for (int i = 0; i < revokedAbilities.size(); i++)
				{
					String revokedString = grantedAbilities.get(i).getAsString();
					if(isAbilityGroup(revokedString))
						entry.revokeAbilityGroup(stripAbilityGroupIndicator(revokedString));
					else
						entry.revokeAbility(revokedString);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
		
		// Resolve the stored data
		this.lazyResolveEntity = () ->
		{
			for(Map.Entry<ResourceLocation, MorphAbilityEntry> entityTagTupel : abilityEntityTagEntries.entrySet())
			{
				TagKey<EntityType<?>> tagKey = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, entityTagTupel.getKey());
				
				// Find every entity that has this tag
//				ForgeRegistries.ENTITIES.tags().getTag(tagKey).forEach(entityType ->
//				{
//					
//				});
				
				if(ForgeRegistries.ENTITIES.tags().isKnownTagName(tagKey))
				{
					ForgeRegistries.ENTITIES.tags().getTag(tagKey).forEach(entityType ->
					{
						MorphAbilityEntry entry = abilityEntries.computeIfAbsent(new MorphAbilityKey(entityType.getRegistryName(), MorphAbilityEntryType.ENTITY), keyLambda -> new MorphAbilityEntry());
						entry.mergeWith(entityTagTupel.getValue());
					});
				}
				else
				{
					LOGGER.warn("Entity Tag {} is not known to the game; discarding the content of this ability file.", entityTagTupel.getKey());
				}
			}
			
			abilityEntries.forEach((entity, entry) ->
			{
				try
				{
					if(entity.type == MorphAbilityEntryType.ENTITY)
					{						
						if(!ForgeRegistries.ENTITIES.containsKey(entity.name))
						{
							LOGGER.warn(String.format("The given entity %s is not known to the game. Skipping this entry. Please make sure to only load this when the mod for the entity is present. You can do this by putting this JSON file in \"data/<modname>/morph_abilities\".", entity));
						}
						else
						{
							EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(entity.name);
							entityAbilityLookup.put(entityType, entry.resolve());
						}
					}
					else
					{
						customAbilityLookup.put(entity.name, entry.resolve());
					}
					
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			});
		};
	}
	
	private static boolean isAbilityGroup(String string)
	{
		return string.startsWith("#");
	}
	
	private static String stripAbilityGroupIndicator(String inputString)
	{
		return inputString.substring(1);
	}
	
	private static enum MorphAbilityEntryType
	{
		ENTITY, CUSTOM
	}
	
	// I should really rewrite this class... It has become almost umaintainable for
	// people who are not versed in this project's code, so the only person
	// understanding this code is me, Budschie.
	private static class MorphAbilityEntry
	{
		private HashSet<String> grantedAbilities = new HashSet<>();
		private HashSet<String> revokedAbilities = new HashSet<>();
		private HashSet<String> grantedAbilityGroups = new HashSet<>();
		private HashSet<String> revokedAbilityGroups = new HashSet<>();
		
		public void grantAbility(String abilityResourceLocation)
		{
			grantedAbilities.add(abilityResourceLocation);
		}
		
		public void revokeAbility(String abilityResourceLocation)
		{
			revokedAbilities.add(abilityResourceLocation);
		}
		
		public void grantAbilityGroup(String abilityGroupResourceLocation)
		{
			grantedAbilityGroups.add(abilityGroupResourceLocation);
		}
		
		public void revokeAbilityGroup(String abilityGroupResourceLocation)
		{
			revokedAbilityGroups.add(abilityGroupResourceLocation);
		}
		
		public List<Ability> resolve()
		{
			resolveAbilityGroup(grantedAbilityGroups, grantedAbilities);
			resolveAbilityGroup(revokedAbilityGroups, revokedAbilities);
			
			return grantedAbilities.stream().filter(granted -> !revokedAbilities.contains(granted)).filter(exists ->
			{
				if(!BMorphMod.DYNAMIC_ABILITY_REGISTRY.hasEntry(new ResourceLocation(exists)))
				{
					LOGGER.warn(String.format("Ability %s does not exist. Please check if every mod is loaded, or if you made a typo. Skipping this ability.", exists));
					return false;
				}
				
				return true;
			}).map(strRaw -> BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(new ResourceLocation(strRaw))).collect(Collectors.toList());
		}
		
		public void mergeWith(MorphAbilityEntry otherEntry)
		{
			grantedAbilities.addAll(otherEntry.grantedAbilities);
			revokedAbilities.addAll(otherEntry.revokedAbilities);
			grantedAbilityGroups.addAll(otherEntry.grantedAbilityGroups);
			revokedAbilityGroups.addAll(otherEntry.revokedAbilityGroups);
		}
		
		private void resolveAbilityGroup(HashSet<String> abilityGroups, HashSet<String> addTo)
		{
			for(String abilityGroup : abilityGroups)
			{
				AbilityGroup grantedAbilityGroupResolved = BMorphMod.ABILITY_GROUPS.getEntry(new ResourceLocation(abilityGroup));
				
				if(grantedAbilityGroupResolved == null)
				{
					LOGGER.warn(MessageFormat.format("Ability group {0} in morph_abilities file doesn't exist. Skipping it.", abilityGroup));
					return;
				}
				
				for(Ability ability : grantedAbilityGroupResolved.getAbilities())
				{
					addTo.add(ability.getResourceLocation().toString());
				}
			}
		}
	}
	
	private static class MorphAbilityKey
	{
		private ResourceLocation name;
		private MorphAbilityEntryType type;
		
		public MorphAbilityKey(ResourceLocation name, MorphAbilityEntryType type)
		{
			this.name = name;
			this.type = type;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof MorphAbilityKey key)
			{
				return key.name.equals(this.name) && key.type == this.type;
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return name.hashCode() ^ type.hashCode();
		}
	}
}
