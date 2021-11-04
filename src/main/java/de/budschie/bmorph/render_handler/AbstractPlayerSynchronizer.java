package de.budschie.bmorph.render_handler;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AbstractPlayerSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof AbstractClientPlayerEntity;
	}
	
	@Override
	public void applyToMorphEntity(Entity morphEntity, PlayerEntity player)
	{
		AbstractClientPlayerEntity entity = (AbstractClientPlayerEntity) morphEntity;

		entity.chasingPosX = player.chasingPosX;
		entity.prevChasingPosX = player.prevChasingPosX;
		entity.chasingPosY = player.chasingPosY;
		entity.prevChasingPosY = player.prevChasingPosY;
		entity.chasingPosZ = player.chasingPosZ;
		entity.prevChasingPosZ = player.prevChasingPosZ;
		
		if (entity.isElytraFlying() != player.isElytraFlying())
		{
			if (player.isElytraFlying())
				entity.startFallFlying();
			else
				entity.stopFallFlying();
		}
	}
}
