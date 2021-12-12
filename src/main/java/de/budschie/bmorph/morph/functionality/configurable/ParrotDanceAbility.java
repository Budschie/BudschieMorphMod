package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.capabilities.parrot_dance.ParrotDanceCapabilityHandler;
import de.budschie.bmorph.capabilities.parrot_dance.ParrotDanceCapabilityInstance;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;

public class ParrotDanceAbility extends Ability
{
	public static final Codec<ParrotDanceAbility> CODEC = Codec.unit(ParrotDanceAbility::new);
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		player.getCapability(ParrotDanceCapabilityInstance.PARROT_CAP).ifPresent(cap ->
		{
			ParrotDanceCapabilityHandler.INSTANCE.setDancingServer(player, !cap.isDancing());
		});
	}
}
