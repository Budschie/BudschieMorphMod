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

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/** https://forums.minecraftforge.net/topic/100915-1165-make-mod-data-editable-with-datapack-adding-new-data-type/ **/
public class MorphAbilityManager extends JsonReloadListener
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).create();
	private HashMap<EntityType<?>, List<Ability>> abilityLookup = new HashMap<>();
	
	public MorphAbilityManager()
	{
		super(GSON, "morph_abilities");
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public List<Ability> getAbilitiesFor(EntityType<?> entity)
	{
		return abilityLookup.get(entity);
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public List<Ability> getAbilitiesFor(MorphItem morphItem)
	{
		return getAbilitiesFor(morphItem.getEntityType());
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn,
			IProfiler profilerIn)
	{
		abilityLookup.clear();
		
		HashMap<String, MorphAbilityEntry> abilityEntries = new HashMap<>();
		
		// Load in all granted and revoked abilities and store this data in an HashMap with a String representing the entity resource location as a 
		// key and a MorphAbilityEntry as a value.
		objectIn.forEach((resourceLocation, json) ->
		{
			if(ModList.get().isLoaded(resourceLocation.getNamespace()))
			{
				try
				{
					JsonObject root = json.getAsJsonObject();
					String entity = root.get("entity_type").getAsString();
					JsonArray grantedAbilities = root.getAsJsonArray("grant");
					JsonArray revokedAbilities = root.getAsJsonArray("revoke");
					
					MorphAbilityEntry entry = abilityEntries.computeIfAbsent(entity, key -> new MorphAbilityEntry());
					
					for(int i = 0; i < grantedAbilities.size(); i++)
					{
						entry.grantAbility(grantedAbilities.get(i).getAsString());
					}
					
					for(int i = 0; i < revokedAbilities.size(); i++)
					{
						entry.revokeAbility(revokedAbilities.get(i).getAsString());
					}
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
		
		// Resolve the stored data
		abilityEntries.forEach((entity, entry) ->
		{
			try
			{
				EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entity));
				
				if(!ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(entity)))
				{
					LOGGER.warn(String.format("The given entity %s is not known to the game. Skipping this entry.", entity));
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
				if(!AbilityRegistry.REGISTRY.get().containsKey(new ResourceLocation(exists)))
				{
					LOGGER.warn(String.format("Ability %s does not exist. Please check if every mod is loaded, or if you made a typo. Skipping this ability.", exists));
					return false;
				}
				
				return true;
			}).map(strRaw -> AbilityRegistry.REGISTRY.get().getValue(new ResourceLocation(strRaw))).collect(Collectors.toList());
		}
	}
}
