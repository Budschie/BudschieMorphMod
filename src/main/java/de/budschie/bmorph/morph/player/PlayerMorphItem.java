package de.budschie.bmorph.morph.player;

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PlayerMorphItem extends MorphItem
{
	GameProfile gameProfile;
	
	public PlayerMorphItem(GameProfile gameProfile)
	{
		super("player_morph_item");
		this.gameProfile = gameProfile;
	}
	
	public PlayerMorphItem()
	{
		super("player_morph_item");
	}
	
	@Override
	public EntityType<?> getEntityType()
	{
		return EntityType.PLAYER;
	}

	@Override
	public Entity createEntity(Level world)
	{		
		// -69420 points for code style... TODO: This is f****** stupid...
		return world.isClientSide ? UglyHackThatDoesntWork.thisisstupid.apply(gameProfile, world) : new Player(world, new BlockPos(0, 0, 0), 0, gameProfile)
		{
			@Override
			public boolean isSpectator()
			{
				return false;
			}
			
			@Override
			public boolean isCreative()
			{
				return false;
			}
		};
	}
	
	@Override
	public boolean isAllowedToPickUp(Player picker)
	{
		return picker != null && !Objects.equal(gameProfile, picker.getGameProfile());
	}

	@Override
	public void deserializeAdditional(CompoundTag nbt)
	{
		gameProfile = new GameProfile(nbt.getUUID("UUID"), nbt.getString("Name"));
	}

	@Override
	public CompoundTag serializeAdditional()
	{
		CompoundTag nbt = new CompoundTag();
		nbt.putUUID("UUID", gameProfile.getId());
		nbt.putString("Name", gameProfile.getName());
		return nbt;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof PlayerMorphItem)
		{
			return MorphManagerHandlers.PLAYER.equalsFor(this, (PlayerMorphItem)obj);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return MorphManagerHandlers.PLAYER.hashCodeFor(this);
	}	
}
