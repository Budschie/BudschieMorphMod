package de.budschie.bmorph.render_handler;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;

// The fact that I have to do this sh*t shows how horrible some parts of MC code really are...
public class ChickenSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Chicken;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Chicken chicken = (Chicken) morphEntity;
		
		boolean groundOrFlying = player.getAbilities().flying || chicken.isOnGround();
		
		// Assign old values
		chicken.oFlapSpeed = chicken.flapSpeed;
		chicken.oFlap = chicken.flap;

		// Change flying speed, accordingly to whether you are flying/on ground or not.
		chicken.flapSpeed += (groundOrFlying ? -1.0f : 4.0f) * 0.3f;
		chicken.flapSpeed = Mth.clamp(chicken.flapSpeed, 0.0f, 1.0f);
		
		if (!groundOrFlying && chicken.flapping < 1.0f)
		{
			chicken.flapping = 1.0f;
		}

		chicken.flapping = chicken.flapping * 0.9f;

		chicken.flap += chicken.flapping * 2.0f;
	}
}
