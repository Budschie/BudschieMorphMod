package de.budschie.bmorph.json_integration;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import de.budschie.bmorph.morph.FallbackMorphManager.SpecialDataHandler;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

public class MorphNBTHandler extends JsonReloadListener
{
	private static final Gson GSON = (new GsonBuilder()).create();
	
	public MorphNBTHandler()
	{
		super(GSON, "morph_nbt");
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn,
			IProfiler profilerIn)
	{
		HashMap<EntityType<?>, SpecialDataHandler> nbtDataHandlers = new HashMap<>();
		
		MorphManagerHandlers.FALLBACK.setDataHandlers(nbtDataHandlers);
		
		objectIn.forEach((resourceLocation, json) ->
		{
			if(ModList.get().isLoaded(resourceLocation.getNamespace()))
			{
				
			}
		});
	}
}
