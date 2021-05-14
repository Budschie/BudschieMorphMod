package de.budschie.bmorph.api_interact;

import net.minecraft.entity.player.PlayerEntity;

public class DummyShrinkAPIInteract implements IShrinkAPIInteract
{
	@Override
	public float getShrinkingValue(PlayerEntity player)
	{
		return 1;
	}

	@Override
	public boolean isShrunk(PlayerEntity player)
	{
		return false;
	}
}
