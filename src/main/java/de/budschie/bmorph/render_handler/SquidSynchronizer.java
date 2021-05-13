package de.budschie.bmorph.render_handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class SquidSynchronizer implements IEntitySynchronizer
{

	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof SquidEntity;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, PlayerEntity player)
	{
		SquidEntity squid = (SquidEntity) morphEntity;
		squid.lastTentacleAngle = squid.tentacleAngle;
		squid.tentacleAngle = MathHelper.abs(MathHelper.sin(squid.squidRotation)) * (float) Math.PI * 0.25F;
		squid.prevSquidRotation = squid.squidRotation;
		squid.squidRotation = (float) Math.sin(System.currentTimeMillis() / 250.0);

		Vector3d squidPitchYaw = Vector3d.fromPitchYaw(player.getPitchYaw());
		squid.prevSquidPitch = squid.squidPitch;
		squid.prevSquidYaw = squid.squidYaw;
		squid.squidPitch = -90;
		squid.squidYaw = (float) squidPitchYaw.y;

	}
}
