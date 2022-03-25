package de.budschie.bmorph.json_integration.ability_groups;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry.AbilityGroup;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class AbilityGroups extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	
	public AbilityGroups()
	{
		super(GSON, "ability_groups");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler)
	{
		BMorphMod.ABILITY_GROUPS.unregisterAll();
		
		ArrayList<Pair<ResourceLocation, ArrayList<ResourceLocation>>> abilityGroupsUnresolved = new ArrayList<>();
		
		pObject.forEach((rl, json) ->
		{
			ArrayList<ResourceLocation> abilityResourceLocations = new ArrayList<>();
			
			JsonArray array = json.getAsJsonObject().get("abilities").getAsJsonArray();
			
			for(int i = 0; i < array.size(); i++)
			{
				abilityResourceLocations.add(new ResourceLocation(array.get(i).getAsString()));
			}
			
			abilityGroupsUnresolved.add(new Pair<>(rl, abilityResourceLocations));
		});
		
		BMorphMod.ABILITY_GROUPS.fillRegistry = () ->
		{			
			for(Pair<ResourceLocation, ArrayList<ResourceLocation>> pair : abilityGroupsUnresolved)
			{
				AbilityGroup abilityGroup = new AbilityGroup(pair.getA());
				
				resolveAbilities:
				for(ResourceLocation unresolvedAbility : pair.getB())
				{
					Ability resolvedAbility = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(unresolvedAbility);
					
					if(resolvedAbility == null)
					{
						LOGGER.warn(MessageFormat.format("The ability {0} in ability group {1} doesn't exist. It will not be included in this ability group. See if you made a typo.", unresolvedAbility, pair.getA()));
						continue resolveAbilities;
					}
					
					abilityGroup.addAbility(resolvedAbility);
				}
				
				BMorphMod.ABILITY_GROUPS.registerEntry(abilityGroup);
			}
		};
	}	
}
