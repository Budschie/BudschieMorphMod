package de.budschie.bmorph.render_handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;

public class ParrotSynchronizer implements IEntitySynchronizer
{

	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof ParrotEntity;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, PlayerEntity player)
	{
		ParrotEntity parrot = (ParrotEntity) morphEntity;
		parrot.partyParrot = true;
	}
}
