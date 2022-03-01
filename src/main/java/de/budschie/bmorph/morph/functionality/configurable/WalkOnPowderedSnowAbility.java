package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.events.CanWalkOnPowderSnowEvent;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WalkOnPowderedSnowAbility extends Ability
{
	public static final Codec<WalkOnPowderedSnowAbility> CODEC = Codec.unit(() -> new WalkOnPowderedSnowAbility());
	
	@SubscribeEvent
	public void onWalkingOnPowderedSnow(CanWalkOnPowderSnowEvent event)
	{
		if(isTracked(event.getEntity()))
			event.setResult(Result.ALLOW);
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
