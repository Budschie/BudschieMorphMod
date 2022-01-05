package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class CommonEntitySynchronizer implements IEntitySynchronizer
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
		
		EntityUtil.copyLocationAndRotation(player, morphEntity);
		
		morphEntity.wasTouchingWater = player.isInWater();
		
		morphEntity.setOnGround(player.isOnGround());
	}
}
