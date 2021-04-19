package de.budschie.bmorph.morph;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

public class PlayerMorphManager implements IMorphManager<PlayerMorphItem, GameProfile>
{
	@Override
	public boolean doesManagerApplyTo(EntityType<?> type)
	{
		return type == EntityType.PLAYER;
	}
	
	@Override
	public PlayerMorphItem createMorphFromEntity(Entity entity)
	{
		if(entity.getType() != EntityType.PLAYER)
			throw new IllegalAccessError("Please only call this method for dead players.");
		
		return createMorph(entity.getType(), ((PlayerEntity)entity).getGameProfile());
	}

	// Sadly I can't remove this :(
	@Deprecated(since = "The beginning lol", forRemoval = false)
	@Override
	public PlayerMorphItem createMorph(EntityType<?> entity, CompoundNBT nbt, GameProfile data)
	{
		if(entity != EntityType.PLAYER)
			throw new IllegalAccessError("Please only call this method for player morphs.");
		
		return new PlayerMorphItem(data);
	}
	
	@Override
	public PlayerMorphItem createMorph(EntityType<?> entity, GameProfile data)
	{
		if(entity != EntityType.PLAYER)
			throw new IllegalAccessError("Please only call this method for player morphs.");
		
		return new PlayerMorphItem(data);
	}

	@Override
	public boolean equalsFor(PlayerMorphItem item1, PlayerMorphItem item2)
	{
		return item1.gameProfile.equals(item2.gameProfile);
	}

	@Override
	public int hashCodeFor(PlayerMorphItem item)
	{
		return item.gameProfile.hashCode();
	}
}
