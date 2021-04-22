package de.budschie.bmorph.morph;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
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
		return UglyHackThatDoesntWork.thisisstupid.apply(gameProfile, world);
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
