package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.entity.player.PlayerEntity;

public class AbilityClientShit
{
	public static void playGuardianSound(PlayerEntity forPlayer)
	{
		// We know that this is a guardian
		Minecraft.getInstance().getSoundHandler().play(new GuardianSound((GuardianEntity) RenderHandler.getCachedEntity(forPlayer)));
	}
}
