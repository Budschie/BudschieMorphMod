package de.budschie.bmorph.api_interact;

import net.minecraft.world.entity.player.Player;

public interface IShrinkAPIInteract
{
	float getShrinkingValue(Player player);
	boolean isShrunk(Player player);
}
