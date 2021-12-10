package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;

public class ParrotSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Parrot;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player, float partialTicks)
	{
		Parrot parrot = (Parrot) morphEntity;
	}
}
