package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AirSuffocationAbility extends AbstractEventAbility
{
	public static final Codec<AirSuffocationAbility> CODEC = ModCodecs.newCodec(AirSuffocationAbility::new);
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			if(isTracked(event.player) && !event.player.isInWaterOrBubble())
			{
				// I don't know why, but I have to subtract 5 for this to work.
				event.player.setAirSupply(event.player.getAirSupply() - 5);
				
				if(event.player.getAirSupply() <= -20)
				{
					event.player.setAirSupply(0);
					
					event.player.hurt(DamageSource.DROWN, 2);
				}
			}
		}
	}
}
