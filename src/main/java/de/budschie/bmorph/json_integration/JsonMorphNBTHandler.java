package de.budschie.bmorph.json_integration;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.fallback.IMorphNBTHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class JsonMorphNBTHandler implements IMorphNBTHandler
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private NBTPath[] trackedNbt;
	private CompoundNBT defaultNbt;
	
	public JsonMorphNBTHandler(CompoundNBT defaultNbt, NBTPath... trackedNbt)
	{
		this.trackedNbt = trackedNbt;
		this.defaultNbt = defaultNbt;
	}
	
	@Override
	public boolean areEquals(FallbackMorphItem item1, FallbackMorphItem item2)
	{
		CompoundNBT fallback1Serialized = item1.serializeAdditional();
		CompoundNBT fallback2Serialized = item2.serializeAdditional();
		
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
		CompoundNBT nbt = item.serializeAdditional();
		
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
	public CompoundNBT applyDefaultNBTData(CompoundNBT in)
	{
		CompoundNBT out = defaultNbt.copy();
		
		// Copy every path from the in compound nbt to the out compound nbt
		for(NBTPath nbtPath : trackedNbt)
			nbtPath.copyTo(in, out);
		
		return out;
	}
	
	public CompoundNBT getDefaultNbt()
	{
		return defaultNbt;
	}
	
	public NBTPath[] getTrackedNbt()
	{
		return trackedNbt;
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
}
