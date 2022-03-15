package de.budschie.bmorph.morph;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class MorphItem
{
	private String morphItemId;
	
	// We will control this vars through a new ability
	private int stunnedUntil = -1;
	private int totalStunTime = 1;
	
	protected MorphItem(String morphItemId)
	{
		this.morphItemId = morphItemId;
	}
	
	public CompoundTag serialize()
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putString("id", getMorphItemId());
		nbt.put("additional", serializeAdditional());
		
		// If we are stunned, save all of this stun stuff
		if(isStunned())
		{
			nbt.putInt("stunned_until", BudschieUtils.convertToRelativeTime(stunnedUntil));
			nbt.putInt("total_stun_time", totalStunTime);
		}
		
		return nbt;
	}
	
	public void deserialize(CompoundTag nbt)
	{
		if(!nbt.getString("id").equals(getMorphItemId()))
			throw new IllegalArgumentException("The wrong morph item is being serialized. Please report this bug to the developer.");
		else
		{
			deserializeAdditional(nbt.getCompound("additional"));
			
			if(nbt.contains("stunned_until", Tag.TAG_INT))
			{
				stunnedUntil = BudschieUtils.convertToAbsoluteTime(nbt.getInt("stunned_until"));
				totalStunTime = nbt.getInt("total_stun_time");
			}
		}
	}
	
	public boolean isStunned()
	{
		return ServerSetup.server.getTickCount() < stunnedUntil;
	}
	
	public float getStunProgress()
	{
		return 1f - (BudschieUtils.convertToRelativeTime(stunnedUntil) / ((float)totalStunTime));
	}
	
	public abstract void deserializeAdditional(CompoundTag nbt);
	public abstract CompoundTag serializeAdditional();
	
	public abstract EntityType<?> getEntityType();
	public abstract Entity createEntity(Level world) throws NullPointerException;
	
	public boolean isAllowedToPickUp(Player picker)
	{
		return true;
	}
	
	public String getMorphItemId()
	{
		return morphItemId;
	}
}
