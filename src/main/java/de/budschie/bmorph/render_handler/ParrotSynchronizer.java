package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.parrot_dance.ParrotDanceCapabilityInstance;
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
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Parrot parrot = (Parrot) morphEntity;
		
		player.getCapability(ParrotDanceCapabilityInstance.PARROT_CAP).ifPresent(cap ->
		{
			parrot.partyParrot = cap.isDancing();
		});
	}
}
