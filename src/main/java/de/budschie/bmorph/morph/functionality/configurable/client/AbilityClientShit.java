package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;

public class AbilityClientShit
{
	public static void playGuardianSound(Player forPlayer)
	{
		// We know that this is a guardian
		Minecraft.getInstance().getSoundManager().play(new GuardianAttackSoundInstance((Guardian) forPlayer.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().get().getOrCreateCachedEntity(forPlayer)));
	}
}
