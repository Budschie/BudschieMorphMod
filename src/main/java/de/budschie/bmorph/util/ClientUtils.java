package de.budschie.bmorph.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientUtils
{
	public static Player getPlayer()
	{
		return Minecraft.getInstance().player;
	}
}
