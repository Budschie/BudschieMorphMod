package de.budschie.bmorph.morph;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public abstract class MorphItem
{
	private String morphItemId;
	
	protected MorphItem(String morphItemId)
	{
		this.morphItemId = morphItemId;
	}
	
	public CompoundTag serialize()
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", getMorphItemId());
		nbt.put("additional", serializeAdditional());
		
		return nbt;
	}
	
	public void deserialize(CompoundTag nbt)
	{
		if(!nbt.getString("id").equals(getMorphItemId()))
			throw new IllegalArgumentException("The wrong morph item is being serialized. Please report this bug to the developer.");
		else
		{
			deserializeAdditional(nbt.getCompound("additional"));
		}
	}
	
	public abstract void deserializeAdditional(CompoundTag nbt);
	public abstract CompoundTag serializeAdditional();
	
	public abstract EntityType<?> getEntityType();
	public abstract Entity createEntity(Level world);
	
	public boolean isAllowedToPickUp(Player picker)
	{
		return true;
	}
	
	public String getMorphItemId()
	{
		return morphItemId;
	}
}
