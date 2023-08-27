package de.budschie.bmorph.datagen.nbt_handlers;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.budschie.bmorph.json_integration.JsonMorphNBTHandler;
import de.budschie.bmorph.json_integration.NBTPath;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class NBTHandlerProvider implements DataProvider
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private HashMap<EntityType<?>, JsonMorphNBTHandler> data = new HashMap<>();
	private PackOutput output;
	
	public NBTHandlerProvider(PackOutput output)
	{
		this.output = output;
	}
		
	@Override
	public CompletableFuture<?> run(CachedOutput pOutput)
	{
		int i = 0;
		CompletableFuture<?>[] futures = new CompletableFuture<?>[data.entrySet().size()];
		
		for(Map.Entry<EntityType<?>, JsonMorphNBTHandler> entry : data.entrySet())
			futures[i++] = DataProvider.saveStable(pOutput, serializeJsonMorphNBTHandler(entry.getKey(), entry.getValue()),
					output.getOutputFolder(Target.DATA_PACK).resolve(FileSystems.getDefault().getPath("data", ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey()).getNamespace(), "morph_nbt",
							ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey()).getPath() + ".json")));
		
		return CompletableFuture.allOf(futures);
	}
	
	private JsonElement serializeJsonMorphNBTHandler(EntityType<?> type, JsonMorphNBTHandler nbtHandler)
	{
		JsonObject root = new JsonObject();
		
		// Set entity type
		root.addProperty("entity_type", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
		
		// Set tracked nbt tags
		JsonArray trackedNbtJSON = new JsonArray();
		
		for(NBTPath nbtPath : nbtHandler.getTrackedNbt())
			trackedNbtJSON.add(nbtPath.toString());
		
		root.add("tracked_nbt_keys", trackedNbtJSON);
		
		// Set default nbt data
		root.addProperty("default_nbt", nbtHandler.getDefaultNbt().getAsString());
		
		return root;
	}
	
	public void addData(EntityType<?> entityType, JsonMorphNBTHandler nbtHandler)
	{
		data.put(entityType, nbtHandler);
	}

	@Override
	public String getName()
	{
		return "Morph NBT Handlers";
	}
}
