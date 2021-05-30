package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;

public class FlyAbility extends Ability
{
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		player.abilities.allowFlying = true;
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		if(!player.isCreative() && !player.isSpectator())
		{
			player.abilities.allowFlying = false;
			player.abilities.isFlying = false;
		}
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
