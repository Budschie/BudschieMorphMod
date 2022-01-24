package de.budschie.bmorph.morph.functionality.codec_addition;

import net.minecraft.nbt.Tag;

public interface IDataModifier
{
	boolean canOperateOn(Tag nbtTag);
	
	// Return a new tag based on the old tag
	Tag applyModifier(Tag inputTag);
}
