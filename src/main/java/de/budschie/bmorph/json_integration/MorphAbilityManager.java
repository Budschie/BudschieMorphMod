package de.budschie.bmorph.json_integration;

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

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/** https://forums.minecraftforge.net/topic/100915-1165-make-mod-data-editable-with-datapack-adding-new-data-type/ **/
public class MorphAbilityManager extends SimpleJsonResourceReloadListener
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).create();
	private HashMap<EntityType<?>, List<Ability>> abilityLookup = new HashMap<>();
	private Runnable lazyResolve;
	
	public MorphAbilityManager()
	{
		super(GSON, "morph_abilities");
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public List<Ability> getAbilitiesFor(EntityType<?> entity)
	{
		if(lazyResolve != null)
		{
			lazyResolve.run();
			lazyResolve = null;
		}
		
		return abilityLookup.get(entity);
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public List<Ability> getAbilitiesFor(MorphItem morphItem)
	{
		return getAbilitiesFor(morphItem.getEntityType());
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn,
			ProfilerFiller profilerIn)
	{
		abilityLookup.clear();
		
		HashMap<String, MorphAbilityEntry> abilityEntries = new HashMap<>();
		
		// Load in all granted and revoked abilities and store this data in an HashMap with a String representing the entity resource location as a 
		// key and a MorphAbilityEntry as a value.
		objectIn.forEach((resourceLocation, json) ->
		{
			try
			{
				JsonObject root = json.getAsJsonObject();
				String entity = root.get("entity_type").getAsString();
				JsonArray grantedAbilities = root.getAsJsonArray("grant");
				JsonArray revokedAbilities = root.getAsJsonArray("revoke");

				MorphAbilityEntry entry = abilityEntries.computeIfAbsent(entity, key -> new MorphAbilityEntry());

				for (int i = 0; i < grantedAbilities.size(); i++)
				{
					entry.grantAbility(grantedAbilities.get(i).getAsString());
				}

				for (int i = 0; i < revokedAbilities.size(); i++)
				{
					entry.revokeAbility(revokedAbilities.get(i).getAsString());
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		});
		
		// Resolve the stored data
		this.lazyResolve = () ->
		{
			abilityEntries.forEach((entity, entry) ->
			{
				try
				{
					EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entity));
					
					if(!ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(entity)))
					{
						LOGGER.warn(String.format("The given entity %s is not known to the game. Skipping this entry. Please make sure to only load this when the mod for the entity is present. You can do this by putting this JSON file in \"data/<modname>/morph_abilities\".", entity));
					}
					else
					{
						List<Ability> resolvedAbilities = entry.resolve();
						abilityLookup.put(entityType, resolvedAbilities);
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			});
		};
	}
	
	private static class MorphAbilityEntry
	{
		private HashSet<String> grantedAbilities = new HashSet<>();
		private HashSet<String> revokedAbilities = new HashSet<>();
		
		public void grantAbility(String abilityResourceLocation)
		{
			grantedAbilities.add(abilityResourceLocation);
		}
		
		public void revokeAbility(String abilityResourceLocation)
		{
			revokedAbilities.add(abilityResourceLocation);
		}
		
		public List<Ability> resolve()
		{
			return grantedAbilities.stream().filter(granted -> !revokedAbilities.contains(granted)).filter(exists ->
			{
				if(!BMorphMod.DYNAMIC_ABILITY_REGISTRY.doesAbilityExist(new ResourceLocation(exists)))
				{
					LOGGER.warn(String.format("Ability %s does not exist. Please check if every mod is loaded, or if you made a typo. Skipping this ability.", exists));
					return false;
				}
				
				return true;
			}).map(strRaw -> BMorphMod.DYNAMIC_ABILITY_REGISTRY.getAbility(new ResourceLocation(strRaw))).collect(Collectors.toList());
		}
	}
}
