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
		
		morphEntity.tickCount = player.tickCount;
		
		morphEntity.setPos(player.getX(), player.getY(), player.getZ());
		morphEntity.xOld = player.xOld;
		morphEntity.yOld = player.yOld;
		morphEntity.zOld = player.zOld;
		
		morphEntity.xo = player.xo;
		morphEntity.yo = player.yo;
		morphEntity.zo = player.zo;
		
		morphEntity.walkDist = player.walkDist;
		morphEntity.walkDistO = player.walkDistO;
		
		morphEntity.wasTouchingWater = player.isInWater();
		
		morphEntity.setOnGround(player.isOnGround());
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
