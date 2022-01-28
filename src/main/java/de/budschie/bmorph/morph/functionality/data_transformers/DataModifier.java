package de.budschie.bmorph.morph.functionality.data_transformers;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public abstract class DataModifier
{
	private DataModifierHolder<? extends DataModifier> dataModifierHolder;
	
	public abstract boolean canOperateOn(Optional<Tag> nbtTag);
	
	// Return a new tag based on the old tag
	public abstract Optional<Tag> applyModifier(Optional<Tag> inputTag);
	
	public void setDataModifierHolder(DataModifierHolder<?> dataModifierHolder)
	{
		this.dataModifierHolder = dataModifierHolder;
	}
	
	public DataModifierHolder<? extends DataModifier> getDataModifierHolder()
	{
		return dataModifierHolder;
	}
	
	public Optional<CompoundTag> serializeNbt()
	{
		return getDataModifierHolder().serializeNBTIAmTooDumbForJava(this);
	}
}
