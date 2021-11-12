package de.budschie.bmorph.morph.fallback;

import net.minecraft.nbt.CompoundTag;

public interface IMorphNBTHandler
{
	/** This method shall return true if item1 and item2 are equal. **/
	boolean areEquals(FallbackMorphItem item1, FallbackMorphItem item2);
	
	/** This method yields code for calculating a hash code for the given morph item. **/
	int getHashCodeFor(FallbackMorphItem item);
	
	/** This method shall return the NBT data for a morph based on the input, which is the entity that was killed for the morph. **/
	CompoundTag applyDefaultNBTData(CompoundTag in);
}
