package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityAttacher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.entity.player.PlayerEntity;

public class PufferfishSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof PufferfishEntity;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, PlayerEntity player)
	{
		PufferfishEntity pufferEntity = (PufferfishEntity) morphEntity;
		
		player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
		{
			pufferEntity.setPuffState(cap.getPuffState());
		});
	}
}
