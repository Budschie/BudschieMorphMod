package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WaterBreathingAbility extends AbstractEventAbility
{
	public static Codec<WaterBreathingAbility> CODEC = ModCodecs.newCodec(WaterBreathingAbility::new);
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(isTracked(event.player) && event.player.isInWater())
		{
			event.player.setAir(14 * 20);
		}
	}
}
