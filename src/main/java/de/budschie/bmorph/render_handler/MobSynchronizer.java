package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class MobSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Mob;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		Mob entity = (Mob) morphEntity;
		entity.setAggressive(true);
	}
}
