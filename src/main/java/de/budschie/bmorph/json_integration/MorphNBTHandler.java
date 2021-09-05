package de.budschie.bmorph.json_integration;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.fallback.IMorphNBTHandler;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphNBTHandler extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	
	public MorphNBTHandler()
	{
		super(GSON, "morph_nbt");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn,
			IProfiler profilerIn)
	{
		HashMap<EntityType<?>, IMorphNBTHandler> nbtDataHandlers = new HashMap<>();
		
		MorphManagerHandlers.FALLBACK.setDataHandlers(nbtDataHandlers);
		
		objectIn.forEach((resourceLocation, json) ->
		{
			try
			{
				if (ModList.get().isLoaded(resourceLocation.getNamespace()))
				{
					JsonObject jsonObject = json.getAsJsonObject();

					String entityTypeString = jsonObject.get("entity_type").getAsString();
					
					// Load in the entity type
					final EntityType<?> entityType = ForgeRegistries.ENTITIES
							.getValue(new ResourceLocation(entityTypeString));
					
					if(entityType == null)
						LOGGER.warn("The entity ", entityTypeString, " doesn't exist. Please make sure to only load this when the mod for the entity is present. You can do this by putting this JSON file in \"data/<modname>/morph_nbt\".");

					// Load in the tracked nbt keys as a NBTPath array
					JsonElement tracked = jsonObject.get("tracked_nbt_keys");
					final NBTPath[] trackedNbtKeys = new NBTPath[tracked == null ? 0 : tracked.getAsJsonArray().size()];

					if(tracked != null)
					{
						for (int i = 0; i < tracked.getAsJsonArray().size(); i++)
							trackedNbtKeys[i] = NBTPath.valueOf(tracked.getAsJsonArray().get(i).getAsString());
					}
					
					JsonElement defaultNBTObject = jsonObject.get("default_nbt");
					
					final CompoundNBT defaultNBT = defaultNBTObject == null ? new CompoundNBT() : new JsonToNBT(new StringReader(defaultNBTObject.getAsString())).readStruct();
					
					// Build SpecialDataHandler
					JsonMorphNBTHandler nbtHandler = new JsonMorphNBTHandler(defaultNBT, trackedNbtKeys);
					
					nbtDataHandlers.put(entityType, nbtHandler);
				}

			} 
			catch (CommandSyntaxException e)
			{
				e.printStackTrace();
			}
		});
		
		MorphNBTHandlersLoadedEvent event = new MorphNBTHandlersLoadedEvent(nbtDataHandlers);
		// Fire an event when we are finished...
		MinecraftForge.EVENT_BUS.post(event);
	}
}
