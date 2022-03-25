package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoFlames extends Ability
{
	public static final Codec<NoFlames> CODEC = Codec.unit(NoFlames::new);
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	@SubscribeEvent
	public void onTick(TickEvent.PlayerTickEvent event)
	{
		if(event.phase == Phase.END && isTracked(event.player))
		{
			event.player.setRemainingFireTicks(0);
		}
	}
}
