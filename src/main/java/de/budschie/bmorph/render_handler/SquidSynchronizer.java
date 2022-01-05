package de.budschie.bmorph.render_handler;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SquidSynchronizer implements IEntitySynchronizer
{

	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Squid;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Squid squid = (Squid) morphEntity;
		squid.oldTentacleAngle = squid.tentacleAngle;
		squid.tentacleAngle = Mth.abs(Mth.sin(squid.tentacleMovement)) * (float) Math.PI * 0.25F;
		squid.oldTentacleMovement = squid.tentacleMovement;
		squid.tentacleMovement = (float) Math.sin(System.currentTimeMillis() / 250.0);

		Vec3 squidPitchYaw = Vec3.directionFromRotation(player.getRotationVector());
		squid.xBodyRotO = squid.xBodyRot;
		squid.zBodyRotO = squid.zBodyRot;
		squid.xBodyRot = -90;
		squid.zBodyRot = (float) squidPitchYaw.y;

	}
}
