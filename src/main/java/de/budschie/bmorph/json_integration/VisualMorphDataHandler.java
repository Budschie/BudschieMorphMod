package de.budschie.bmorph.json_integration;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.VisualMorphDataRegistry.VisualMorphData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** This class is responsible for loading the scales for different morphs from the "morph_visuals" directory. **/
public class VisualMorphDataHandler extends SimpleJsonResourceReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Logger LOGGER = LogManager.getLogger();
	
	public VisualMorphDataHandler()
	{
		super(GSON, "morph_visuals");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn)
	{
		BMorphMod.VISUAL_MORPH_DATA.clear();
		
		objectIn.forEach((resourceLocation, jsonElement) ->
		{
			ResourceLocation forMorph = new ResourceLocation(jsonElement.getAsJsonObject().get("for_morph").getAsString());
			JsonElement scaleJSON = jsonElement.getAsJsonObject().get("morph_ui_scale");
			
			float scale = scaleJSON == null ? 1 : scaleJSON.getAsFloat();
			
			VisualMorphData visualMorphData = new VisualMorphData(scale);
			visualMorphData.setRegistryName(forMorph);
			
			try
			{
				BMorphMod.VISUAL_MORPH_DATA.addVisualMorphData(visualMorphData);
			}
			catch(IllegalArgumentException ex)
			{
				LOGGER.warn(String.format("Morph visual data %s for morph %s already exists. Please choose a different morph.", resourceLocation, forMorph));
			}
		});
	}
}
