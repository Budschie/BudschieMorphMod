package de.budschie.bmorph.datagen.nbt_handlers;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.budschie.bmorph.json_integration.JsonMorphNBTHandler;
import de.budschie.bmorph.json_integration.NBTPath;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.entity.EntityType;

public class NBTHandlerProvider implements DataProvider
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private HashMap<EntityType<?>, JsonMorphNBTHandler> data = new HashMap<>();
	private DataGenerator generator;
	
	public NBTHandlerProvider(DataGenerator generator)
	{
		this.generator = generator;
	}
	
	@Override
	public void run(HashCache cache) throws IOException
	{
		for(Map.Entry<EntityType<?>, JsonMorphNBTHandler> entry : data.entrySet())
			DataProvider.save(GSON, cache, serializeJsonMorphNBTHandler(entry.getKey(), entry.getValue()), generator.getOutputFolder().resolve(FileSystems.getDefault().getPath("data", entry.getKey().getRegistryName().getNamespace(), "morph_nbt", entry.getKey().getRegistryName().getPath() + ".json")));
	}
	
	private JsonElement serializeJsonMorphNBTHandler(EntityType<?> type, JsonMorphNBTHandler nbtHandler)
	{
		JsonObject root = new JsonObject();
		
		// Set entity type
		root.addProperty("entity_type", type.getRegistryName().toString());
		
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
