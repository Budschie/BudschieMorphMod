package de.budschie.bmorph.json_integration;

import java.util.List;

import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.fallback.IMorphNBTHandler;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;

public class JsonMorphNBTHandler implements IMorphNBTHandler
{
//	private static final Logger LOGGER = LogManager.getLogger();
	
	private NBTPath[] trackedNbt;
	private List<LazyOptional<DataTransformer>> dataTransformers;
	private CompoundTag defaultNbt;
	
	public JsonMorphNBTHandler(CompoundTag defaultNbt, NBTPath[] trackedNbt, List<LazyOptional<DataTransformer>> dataTransformers)
	{
		this.defaultNbt = defaultNbt;
		this.trackedNbt = trackedNbt;
		this.dataTransformers = dataTransformers;
	}
	
	@Override
	public boolean areEquals(FallbackMorphItem item1, FallbackMorphItem item2)
	{
		CompoundTag fallback1Serialized = item1.serializeAdditional();
		CompoundTag fallback2Serialized = item2.serializeAdditional();
		
//		for(NBTPath path : trackedNbt)
//		{
//			if(!isINBTDataEqual(path.resolve(fallback1Serialized), path.resolve(fallback2Serialized)))
//				return false;
//		}
		
		// This better work...
		return fallback1Serialized.equals(fallback2Serialized);
	}

	@Override
	public int getHashCodeFor(FallbackMorphItem item)
	{
		int hashCode = item.getEntityType().getRegistryName().toString().hashCode();
		CompoundTag nbt = item.serializeAdditional();
		
//		// Generate a hash code for every nbt element 
//		for(NBTPath path : trackedNbt)
//		{
//			Optional<Integer> nbtHash = getNBTHashCode(path.resolve(nbt));
//			if(nbtHash.isPresent())
//				hashCode ^= nbtHash.get();
//		}
		
		// Let's just hope this works...
		return nbt.hashCode() ^ hashCode;
	}

	@Override
	public CompoundTag applyDefaultNBTData(CompoundTag in)
	{
		CompoundTag out = defaultNbt.copy();
		
		// Copy every path from the in compound nbt to the out compound nbt
		for(NBTPath nbtPath : trackedNbt)
			nbtPath.copyTo(in, out);
		
		// Transform the data
		for(LazyOptional<DataTransformer> transformer : this.dataTransformers)
			transformer.resolve().get().transformData(in, out);
		
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
	
//	private static Object getNBTObject(Tag nbt)
//	{
//		if(nbt == null)
//			return null;
//		
//		if(nbt.getId() == Tag.TAG_INT)
//			return Integer.valueOf(((NumericTag)nbt).getAsInt());
//		else if(nbt.getId() == Tag.TAG_STRING)
//			return ((StringTag)nbt).getAsString();
//		else if(nbt.getId() == Tag.TAG_BYTE)
//			return Byte.valueOf(((NumericTag)nbt).getAsByte());
//		else if(nbt.getId() == Tag.TAG_LONG)
//			return Long.valueOf(((NumericTag)nbt).getAsLong());
//		else
//		{
//			LOGGER.debug("Encountered rare tag with no value handling. Converting tag to string...");
//			return nbt.getAsString();
//		}
//	}
	
//	private static boolean isINBTDataEqual(Tag nbt1, Tag nbt2)
//	{
//		Object nbtObject1 = getNBTObject(nbt1);
//		return nbtObject1 == null ? (nbt2 == null ? true : false) : nbtObject1.equals(getNBTObject(nbt2));
//	}
//	
//	private static Optional<Integer> getNBTHashCode(Tag nbt)
//	{
//		Object nbtObject = getNBTObject(nbt);
//		return nbtObject == null ? Optional.empty() : Optional.of(nbtObject.hashCode());
//	}
}
