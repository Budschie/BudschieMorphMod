package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface IEntitySynchronizerWithRotation extends IEntitySynchronizer
{
	// We need this because else the player rotation in the inventory gets broken
	void updateMorphRotation(Entity morphEntity, Player player);
}
