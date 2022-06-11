package de.budschie.bmorph.render_handler;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EnderDragonSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof EnderDragon;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		EnderDragon dragon = (EnderDragon) morphEntity;

		Vec3 playerMovement = player.getDeltaMovement();
		float flapStrength = 0.2F / ((float) playerMovement.horizontalDistance() * 5.0F + 2.0F);
		flapStrength *= (float) Math.pow(2.0D, playerMovement.y);

		dragon.oFlapTime = dragon.flapTime;
		dragon.flapTime += flapStrength;

		// Update rotation stuff
		if (dragon.posPointer < 0)
		{
			for (int i = 0; i < dragon.positions.length; ++i)
			{
				dragon.positions[i][0] = dragon.getYRot();
				dragon.positions[i][1] = dragon.getY();
			}
		}

		if (++dragon.posPointer == dragon.positions.length)
		{
			dragon.posPointer = 0;
		}

		dragon.positions[dragon.posPointer][0] = dragon.getYRot() + 180;
		dragon.positions[dragon.posPointer][1] = dragon.getY();
		if (dragon.level.isClientSide)
		{
			if (dragon.lerpSteps > 0)
			{
				double d6 = dragon.getX() + (dragon.lerpX - dragon.getX()) / dragon.lerpSteps;
				double d0 = dragon.getY() + (dragon.lerpY - dragon.getY()) / dragon.lerpSteps;
				double d1 = dragon.getZ() + (dragon.lerpZ - dragon.getZ()) / dragon.lerpSteps;
				double d2 = Mth.wrapDegrees(dragon.lerpYRot - dragon.getYRot());
				dragon.setYRot(dragon.getYRot() + (float) d2 / dragon.lerpSteps + 90);
				dragon.setXRot(dragon.getXRot() + (float) (dragon.lerpXRot - dragon.getXRot()) / dragon.lerpSteps);
				--dragon.lerpSteps;
				dragon.setPos(d6, d0, d1);
				dragon.setRot(dragon.getYRot(), dragon.getXRot());
			}
		}
		
		if(dragon.isFlapping())
			dragon.onFlap();
	}
}
