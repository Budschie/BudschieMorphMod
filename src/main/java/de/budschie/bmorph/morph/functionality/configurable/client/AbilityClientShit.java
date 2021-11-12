package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;

public class AbilityClientShit
{
	public static void playGuardianSound(Player forPlayer)
	{
		// We know that this is a guardian
		Minecraft.getInstance().getSoundManager().play(new GuardianAttackSoundInstance((Guardian) RenderHandler.getCachedEntity(forPlayer)));
	}
}
