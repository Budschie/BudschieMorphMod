package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;

public class InstaJumpAbility extends Ability
{

	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		player.stepHeight = 1.45f;
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.stepHeight = 0.6f;
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
