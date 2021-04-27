package de.budschie.bmorph.morph;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public abstract class MorphItem
{
	private String morphItemId;
	
	 MorphItem(String morphItemId)
	{
		this.morphItemId = morphItemId;
	}
	
	public CompoundNBT serialize()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("id", getMorphItemId());
		nbt.put("additional", serializeAdditional());
		
		return nbt;
	}
	
	public void deserialize(CompoundNBT nbt)
	{
		if(!nbt.getString("id").equals(getMorphItemId()))
			throw new IllegalArgumentException("The wrong morph item is being serialized. Please report this bug to the developer.");
		else
		{
			deserializeAdditional(nbt.getCompound("additional"));
		}
	}
	
	public abstract void deserializeAdditional(CompoundNBT nbt);
	public abstract CompoundNBT serializeAdditional();
	
	public abstract EntityType<?> getEntityType();
	public abstract Entity createEntity(World world);
	
	public boolean isAllowedToPickUp(PlayerEntity picker)
	{
		return true;
	}
	
	public String getMorphItemId()
	{
		return morphItemId;
	}
}
