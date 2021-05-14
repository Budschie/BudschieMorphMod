package de.budschie.bmorph.api_interact;

import net.minecraft.entity.player.PlayerEntity;

public interface IShrinkAPIInteract
{
	float getShrinkingValue(PlayerEntity player);
	boolean isShrunk(PlayerEntity player);
}
