package de.budschie.bmorph.json_integration;

import java.util.ArrayList;
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

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.fallback.IMorphNBTHandler;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

public class MorphNBTHandler extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	
	public MorphNBTHandler()
	{
		super(GSON, "morph_nbt");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
	{
		HashMap<EntityType<?>, IMorphNBTHandler> nbtDataHandlers = new HashMap<>();

		MorphManagerHandlers.FALLBACK.setDataHandlers(nbtDataHandlers);

		objectIn.forEach((resourceLocation, json) ->
		{
			try
			{
				JsonObject jsonObject = json.getAsJsonObject();

				String entityTypeString = jsonObject.get("entity_type").getAsString();

				// Load in the entity type
				final EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityTypeString));

				if (entityType == null)
					LOGGER.warn("The entity ", entityTypeString,
							" doesn't exist. Please make sure to only load this when the mod for the entity is present. You can do this by putting this JSON file in \"data/<modname>/morph_nbt\".");

				// Load in the tracked nbt keys as a NBTPath array
				JsonElement tracked = jsonObject.get("tracked_nbt_keys");
				final NBTPath[] trackedNbtKeys = new NBTPath[tracked == null ? 1 : tracked.getAsJsonArray().size() + 1];

				if (tracked != null)
				{
					for (int i = 0; i < tracked.getAsJsonArray().size(); i++)
						trackedNbtKeys[i] = NBTPath.valueOf(tracked.getAsJsonArray().get(i).getAsString());
				}

				JsonElement dataTransformersArrayElement = jsonObject.get("data_transformers");
				ArrayList<LazyOptional<DataTransformer>> dataTransformers = new ArrayList<>();

				if (dataTransformersArrayElement != null)
				{
					for (JsonElement dataTransformerName : dataTransformersArrayElement.getAsJsonArray())
					{
						final ResourceLocation toUse = new ResourceLocation(dataTransformerName.getAsString());
						dataTransformers.add(LazyOptional.of(() -> BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.getEntry(toUse)));
					}
				}

				trackedNbtKeys[trackedNbtKeys.length - 1] = new NBTPath("CustomName");

				JsonElement defaultNBTObject = jsonObject.get("default_nbt");

				final CompoundTag defaultNBT = defaultNBTObject == null ? new CompoundTag()
						: new TagParser(new StringReader(defaultNBTObject.getAsString())).readStruct();

				// Build SpecialDataHandler
				JsonMorphNBTHandler nbtHandler = new JsonMorphNBTHandler(defaultNBT, trackedNbtKeys, dataTransformers);

				nbtDataHandlers.put(entityType, nbtHandler);
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
