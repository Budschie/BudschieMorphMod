package de.budschie.bmorph.morph.functionality;

import java.util.function.Function;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

// Yeet is a beautiful word
public class YeetAbility extends StunAbility
{
	// I LOVE THIS WORD
	Function<PlayerEntity, Entity> yeet;
	float yeetStrength;
	
	public YeetAbility(int stun, Function<PlayerEntity, Entity>  yeet, float yeetStrength)
	{
		super(stun);
		this.yeet = yeet;
		this.yeetStrength = yeetStrength;
	}

	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
			stun(player.getUniqueID());
			
			Entity createdEntity = yeet.apply(player);
			createdEntity.setPosition(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
			createdEntity.setMotion(Vector3d.fromPitchYaw(player.getPitchYaw()).mul(yeetStrength, yeetStrength, yeetStrength));
			player.world.addEntity(createdEntity);
		}
	}
}
