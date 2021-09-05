package de.budschie.bmorph.json_integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import de.budschie.bmorph.morph.FallbackMorphManager.SpecialDataHandler;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
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
	
	private static Object getNBTObject(INBT nbt)
	{
		if(nbt == null)
			return null;
		
		if(nbt.getId() == NBT.TAG_INT)
			return Integer.valueOf(((NumberNBT)nbt).getInt());
		else if(nbt.getId() == NBT.TAG_STRING)
			return ((StringNBT)nbt).getString();
		else if(nbt.getId() == NBT.TAG_BYTE)
			return Byte.valueOf(((NumberNBT)nbt).getByte());
		else if(nbt.getId() == NBT.TAG_LONG)
			return Long.valueOf(((NumberNBT)nbt).getLong());
		else
		{
			LOGGER.debug("Encountered rare tag with no value handling. Converting tag to string...");
			return nbt.getString();
		}
	}
	
	private static boolean isINBTDataEqual(INBT nbt1, INBT nbt2)
	{
		Object nbtObject1 = getNBTObject(nbt1);
		return nbtObject1 == null ? (nbt2 == null ? true : false) : nbtObject1.equals(getNBTObject(nbt2));
	}
	
	private static Optional<Integer> getNBTHashCode(INBT nbt)
	{
		Object nbtObject = getNBTObject(nbt);
		return nbtObject == null ? Optional.empty() : Optional.of(nbtObject.hashCode());
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn,
			IProfiler profilerIn)
	{
		HashMap<EntityType<?>, SpecialDataHandler> nbtDataHandlers = new HashMap<>();
		
		MorphManagerHandlers.FALLBACK.setDataHandlers(nbtDataHandlers);
		
		objectIn.forEach((resourceLocation, json) ->
		{
			try
			{
				if (ModList.get().isLoaded(resourceLocation.getNamespace()))
				{
					JsonObject jsonObject = json.getAsJsonObject();
					System.out.println(jsonObject.toString());

					String entityTypeString = jsonObject.get("entity_type").getAsString();
					
					// Load in the entity type
					final EntityType<?> entityType = ForgeRegistries.ENTITIES
							.getValue(new ResourceLocation(entityTypeString));
					
					if(entityType == null)
						LOGGER.warn("The entity ", entityTypeString, " doesn't exist. Please make sure to only load this when the mod for the entity is present. You can do this by putting this JSON file in \"data/<modname>/morph_nbt\".");

					// Load in the tracked nbt keys as a NBTPath array
					JsonArray tracked = jsonObject.get("tracked_nbt_keys").getAsJsonArray();
					final NBTPath[] trackedNbtKeys = new NBTPath[tracked.size()];

					for (int i = 0; i < tracked.size(); i++)
						trackedNbtKeys[i] = NBTPath.valueOf(tracked.get(i).getAsString());
					
					JsonElement defaultNBTObject = jsonObject.get("default_nbt");
					
					final CompoundNBT defaultNBT = defaultNBTObject == null ? new CompoundNBT() : JsonToNBT.getTagFromJson(defaultNBTObject.getAsString());
					
					// Build SpecialDataHandler
					SpecialDataHandler dataHandler = new SpecialDataHandler((fallback1, fallback2) ->
					{
						CompoundNBT fallback1Serialized = fallback1.serializeAdditional();
						CompoundNBT fallback2Serialized = fallback2.serializeAdditional();
						
						for(NBTPath path : trackedNbtKeys)
						{
							if(!isINBTDataEqual(path.resolve(fallback1Serialized), path.resolve(fallback2Serialized)))
								return false;
						}
						
						return true;
					}, (typeOfFallback, nbt) ->
					{
						int hashCode = typeOfFallback.getRegistryName().toString().hashCode();
						
						// Generate a hash code for every nbt element 
						for(NBTPath path : trackedNbtKeys)
						{
							Optional<Integer> nbtHash = getNBTHashCode(path.resolve(nbt));
							if(nbtHash.isPresent())
								hashCode ^= nbtHash.get();
						}
						
						return hashCode;
					}, in ->
					{
						CompoundNBT out = defaultNBT.copy();
						
						// Copy every path from the in compound nbt to the out compound nbt
						for(NBTPath nbtPath : trackedNbtKeys)
							nbtPath.copyTo(in, out);
						
						return out;
					});
					
					nbtDataHandlers.put(entityType, dataHandler);
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
