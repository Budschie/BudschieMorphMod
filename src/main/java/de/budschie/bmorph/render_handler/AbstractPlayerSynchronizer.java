package de.budschie.bmorph.render_handler;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AbstractPlayerSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof AbstractClientPlayer;
	}
	
	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		AbstractClientPlayer entity = (AbstractClientPlayer) morphEntity;

		entity.xCloak = player.xCloak;
		entity.xCloakO = player.xCloakO;
		entity.yCloak = player.yCloak;
		entity.yCloakO = player.yCloakO;
		entity.zCloak = player.zCloak;
		entity.zCloakO = player.zCloakO;
			
		entity.bob = player.bob;
		entity.oBob = player.oBob;
		
		if (entity.isFallFlying() != player.isFallFlying())
		{
			if (player.isFallFlying())
				entity.startFallFlying();
			else
				entity.stopFallFlying();
		}
	}
}
