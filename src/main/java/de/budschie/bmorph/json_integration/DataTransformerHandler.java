package de.budschie.bmorph.json_integration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.data_transformers.DataModifier;
import de.budschie.bmorph.morph.functionality.data_transformers.DataModifierHolder;
import de.budschie.bmorph.morph.functionality.data_transformers.DataModifierRegistry;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class DataTransformerHandler extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	
	public DataTransformerHandler()
	{
		super(GSON, "data_transformers");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
	{
		BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.unregisterAll();
		
		objectIn.forEach((resourceLocation, jsonElement) ->
		{
			JsonObject rootObject = jsonElement.getAsJsonObject();
			
			NBTPath srcPath = NBTPath.valueOf(rootObject.get("nbt_source").getAsString());
			NBTPath destPath = NBTPath.valueOf(rootObject.get("nbt_destination").getAsString());
			
			ArrayList<DataModifier> dataModifiers = new ArrayList<>();
			
			JsonArray modifiers = rootObject.get("modifiers").getAsJsonArray();
			
			for(JsonElement modifier : modifiers)
			{
				JsonObject modifierCasted = modifier.getAsJsonObject();
				
				ResourceLocation dataModifierName = new ResourceLocation(modifierCasted.get("modifier").getAsString());
				
				if(DataModifierRegistry.REGISTRY.get().containsKey(dataModifierName))
				{
					DataModifierHolder<? extends DataModifier> dataModifierHolder = DataModifierRegistry.REGISTRY.get().getValue(dataModifierName);
					
					Optional<? extends DataModifier> dataModifier = dataModifierHolder.deserializeJson(modifierCasted.get("config"));
					
					if(dataModifier.isPresent())
					{
						dataModifiers.add(dataModifier.get());
					}
					else
					{
						LOGGER.warn(MessageFormat.format("Data modifier with the id {0} in the data transformer {1} could not be parsed. See logs above for further info.", dataModifierName, resourceLocation));
					}
				}
				else
				{
					LOGGER.warn(MessageFormat.format("Data modifier with the id {0} in the data transformer {1} is unknown.", dataModifierName, resourceLocation));
				}
			}
			
			DataTransformer dataTransformer = new DataTransformer(srcPath, destPath, dataModifiers);
			dataTransformer.setResourceLocation(resourceLocation);
			
			BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.registerEntry(dataTransformer);
		});
	}
}
