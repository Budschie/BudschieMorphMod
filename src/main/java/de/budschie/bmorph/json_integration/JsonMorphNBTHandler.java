package de.budschie.bmorph.json_integration;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.fallback.IMorphNBTHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class JsonMorphNBTHandler implements IMorphNBTHandler
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private NBTPath[] trackedNbt;
	private CompoundTag defaultNbt;
	
	public JsonMorphNBTHandler(CompoundTag defaultNbt, NBTPath... trackedNbt)
	{
		this.trackedNbt = trackedNbt;
		this.defaultNbt = defaultNbt;
	}
	
	@Override
	public boolean areEquals(FallbackMorphItem item1, FallbackMorphItem item2)
	{
		CompoundTag fallback1Serialized = item1.serializeAdditional();
		CompoundTag fallback2Serialized = item2.serializeAdditional();
		
		for(NBTPath path : trackedNbt)
		{
			if(!isINBTDataEqual(path.resolve(fallback1Serialized), path.resolve(fallback2Serialized)))
				return false;
		}
		
		return true;
	}

	@Override
	public int getHashCodeFor(FallbackMorphItem item)
	{
		int hashCode = item.getEntityType().getRegistryName().toString().hashCode();
		CompoundTag nbt = item.serializeAdditional();
		
		// Generate a hash code for every nbt element 
		for(NBTPath path : trackedNbt)
		{
			Optional<Integer> nbtHash = getNBTHashCode(path.resolve(nbt));
			if(nbtHash.isPresent())
				hashCode ^= nbtHash.get();
		}
		
		return hashCode;
	}

	@Override
	public CompoundTag applyDefaultNBTData(CompoundTag in)
	{
		CompoundTag out = defaultNbt.copy();
		
		// Copy every path from the in compound nbt to the out compound nbt
		for(NBTPath nbtPath : trackedNbt)
			nbtPath.copyTo(in, out);
		
		return out;
	}
	
	public CompoundTag getDefaultNbt()
	{
		return defaultNbt;
	}
	
	public NBTPath[] getTrackedNbt()
	{
		return trackedNbt;
	}
	
	private static Object getNBTObject(Tag nbt)
	{
		if(nbt == null)
			return null;
		
		if(nbt.getId() == Tag.TAG_INT)
			return Integer.valueOf(((NumericTag)nbt).getAsInt());
		else if(nbt.getId() == Tag.TAG_STRING)
			return ((StringTag)nbt).getAsString();
		else if(nbt.getId() == Tag.TAG_BYTE)
			return Byte.valueOf(((NumericTag)nbt).getAsByte());
		else if(nbt.getId() == Tag.TAG_LONG)
			return Long.valueOf(((NumericTag)nbt).getAsLong());
		else
		{
			LOGGER.debug("Encountered rare tag with no value handling. Converting tag to string...");
			return nbt.getAsString();
		}
	}
	
	private static boolean isINBTDataEqual(Tag nbt1, Tag nbt2)
	{
		Object nbtObject1 = getNBTObject(nbt1);
		return nbtObject1 == null ? (nbt2 == null ? true : false) : nbtObject1.equals(getNBTObject(nbt2));
	}
	
	private static Optional<Integer> getNBTHashCode(Tag nbt)
	{
		Object nbtObject = getNBTObject(nbt);
		return nbtObject == null ? Optional.empty() : Optional.of(nbtObject.hashCode());
	}
}
