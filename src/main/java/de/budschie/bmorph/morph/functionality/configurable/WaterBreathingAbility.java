package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WaterBreathingAbility extends Ability
{
	public static final Codec<WaterBreathingAbility> CODEC = Codec.unit(WaterBreathingAbility::new);
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			if(isTracked(event.player) && event.player.isInWater())
			{
				event.player.setAirSupply(14 * 20);
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
