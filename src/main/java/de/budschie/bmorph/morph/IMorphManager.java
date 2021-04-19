package de.budschie.bmorph.morph;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;

public interface IMorphManager<T extends MorphItem, D>
{
	boolean doesManagerApplyTo(EntityType<?> type);
	
	T createMorphFromEntity(Entity entity);
	
	T createMorph(EntityType<?> entity, CompoundNBT nbt, D data);
	T createMorph(EntityType<?> entity, D data);
	
	boolean equalsFor(T item1, T item2);
	int hashCodeFor(T item);
}
