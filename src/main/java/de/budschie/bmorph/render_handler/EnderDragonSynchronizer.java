package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;

public class EnderDragonSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof EnderDragon;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		EnderDragon dragon = (EnderDragon) morphEntity;

		dragon.setRot(dragon.getYRot() + 180f, dragon.getXRot());
	}
}
