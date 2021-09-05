package de.budschie.bmorph.morph.player;

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	public Entity createEntity(World world)
	{		
		// -69420 points for code style... TODO: This is f****** stupid...
		return world.isRemote ? UglyHackThatDoesntWork.thisisstupid.apply(gameProfile, world) : new PlayerEntity(world, new BlockPos(0, 0, 0), 0, gameProfile)
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
	public boolean isAllowedToPickUp(PlayerEntity picker)
	{
		return picker != null && !Objects.equal(gameProfile, picker.getGameProfile());
	}

	@Override
	public void deserializeAdditional(CompoundNBT nbt)
	{
		gameProfile = new GameProfile(nbt.getUniqueId("UUID"), nbt.getString("Name"));
	}

	@Override
	public CompoundNBT serializeAdditional()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putUniqueId("UUID", gameProfile.getId());
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
