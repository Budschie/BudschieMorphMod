package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

public class TamableSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof TamableAnimal;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		TamableAnimal animal = (TamableAnimal) morphEntity;
		
		animal.setInSittingPose(player.isCrouching());
	}
}
