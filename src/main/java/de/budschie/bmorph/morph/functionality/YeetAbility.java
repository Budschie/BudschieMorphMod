package de.budschie.bmorph.morph.functionality;

import java.util.function.Function;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

// Yeet is a beautiful word
public class YeetAbility extends StunAbility
{
	// I LOVE THIS WORD
	Function<Player, Entity> yeet;
	float yeetStrength;
	
	public YeetAbility(int stun, Function<Player, Entity>  yeet, float yeetStrength)
	{
		super(stun);
		this.yeet = yeet;
		this.yeetStrength = yeetStrength;
	}

	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
	}

	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
			stun(player.getUUID());
			
			Entity createdEntity = yeet.apply(player);
			createdEntity.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
			createdEntity.setDeltaMovement(Vec3.directionFromRotation(player.getRotationVector()).multiply(yeetStrength, yeetStrength, yeetStrength));
			player.level.addFreshEntity(createdEntity);
		}
	}
}
