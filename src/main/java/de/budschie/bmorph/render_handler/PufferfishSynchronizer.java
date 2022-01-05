package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;

public class PufferfishSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Pufferfish;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Pufferfish pufferEntity = (Pufferfish) morphEntity;
		
		player.getCapability(PufferfishCapabilityInstance.PUFFER_CAP).ifPresent(cap ->
		{
			pufferEntity.setPuffState(cap.getPuffState());
		});
	}
}
