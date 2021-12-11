package de.budschie.bmorph.attributes;

import net.minecraft.world.entity.player.Player;

public class AttributeUtil
{
	public static double getAttackRange(Player player)
	{
		double value = player.getAttribute(BMorphAttributes.ATTACK_RANGE.get()).getValue();
		
		return player.isCreative() ? value + 2D : value;
	}
}
