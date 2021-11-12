package de.budschie.bmorph.api_interact;

import net.minecraft.world.entity.player.Player;

public class DummyShrinkAPIInteract implements IShrinkAPIInteract
{
	@Override
	public float getShrinkingValue(Player player)
	{
		return 1;
	}

	@Override
	public boolean isShrunk(Player player)
	{
		return false;
	}
}
