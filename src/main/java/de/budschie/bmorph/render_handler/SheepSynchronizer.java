package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.sheep.SheepCapabilityInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;

public class SheepSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Sheep;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Sheep sheepEntity = (Sheep) morphEntity;
		
		player.getCapability(SheepCapabilityInstance.SHEEP_CAP).ifPresent(cap ->
		{
			sheepEntity.setSheared(cap.isSheared());
		});
	}
}
