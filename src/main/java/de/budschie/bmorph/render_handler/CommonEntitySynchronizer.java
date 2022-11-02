package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class CommonEntitySynchronizer implements IEntitySynchronizerWithRotation
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return true;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		if(morphEntity.level != player.level)
		{
			morphEntity.level = player.level;
		}
		
		morphEntity.xo = player.xo;
		morphEntity.yo = player.yo;
		morphEntity.zo = player.zo;
		morphEntity.setPos(player.getX(), player.getY(), player.getZ());
		morphEntity.xOld = player.xOld;
		morphEntity.yOld = player.yOld;
		morphEntity.zOld = player.zOld;
		
		morphEntity.tickCount = player.tickCount;

		morphEntity.wasTouchingWater = player.isInWater();
		
		morphEntity.setOnGround(player.isOnGround());
		
		morphEntity.vehicle = player.vehicle;
		
		updateMorphRotation(morphEntity, player);
	}
	
	@Override
	public void applyToMorphEntityPostTick(Entity morphEntity, Player player)
	{

		morphEntity.walkDist = player.walkDist;
		morphEntity.walkDistO = player.walkDistO;
	}

	@Override
	public void updateMorphRotation(Entity morphEntity, Player player)
	{
		morphEntity.setXRot(player.getXRot());
		morphEntity.setYBodyRot(player.yBodyRot);
		morphEntity.setYRot(player.getYRot());
		morphEntity.xRotO = player.xRotO;
		morphEntity.yRotO = player.yRotO;
	}
}
