package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;

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
		
	}
	
	@Override
	public void applyToMorphEntityPostTick(Entity morphEntity, Player player)
	{
		Squid squid = (Squid) morphEntity;
		
		if(squid.tentacleMovement > (Math.PI * 2))
		{
			squid.tentacleMovement = 0;
		}
	}
}
