package de.budschie.bmorph.morph;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;

public interface IMorphManager<T extends MorphItem, D>
{
	boolean doesManagerApplyTo(EntityType<?> type);
	
	T createMorphFromEntity(Entity entity);
	
	T createMorph(EntityType<?> entity, CompoundTag nbt, D data, boolean forceNBT);
	
	default T createMorph(EntityType<?> entity, CompoundTag nbt, D data)
	{
		return createMorph(entity, nbt, data, false);
	}
	
	T createMorph(EntityType<?> entity, D data);
	
	boolean equalsFor(T item1, T item2);
	int hashCodeFor(T item);
}
