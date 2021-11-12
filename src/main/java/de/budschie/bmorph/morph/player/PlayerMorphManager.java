package de.budschie.bmorph.morph.player;

import com.mojang.authlib.GameProfile;

import de.budschie.bmorph.morph.IMorphManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

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
		
		return createMorph(entity.getType(), ((Player)entity).getGameProfile());
	}

	// Sadly I can't remove this :(
	//@Deprecated(since = "The beginning lol", forRemoval = false)
	@Deprecated
	@Override
	public PlayerMorphItem createMorph(EntityType<?> entity, CompoundTag nbt, GameProfile data, boolean forceNBT)
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
