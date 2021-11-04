package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FOVChangeAbility extends AbstractEventAbility
{
	public static Codec<FOVChangeAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.fieldOf("fov_change_multiplier").forGetter(FOVChangeAbility::getAmount))
			.apply(instance, FOVChangeAbility::new)); 
	
	private float amount;
	
	public FOVChangeAbility(float amount)
	{
		this.amount = amount;
	}
	
	public float getAmount()
	{
		return amount;
	}
	
	// TODO: Remove crash on server that **will** happen
	@SubscribeEvent
	public void onFOVChangedEvent(FOVUpdateEvent event)
	{
		if(isTracked(event.getEntity()))
			event.setNewfov(event.getFov() * (event.getEntity().isSprinting() ? amount : 1));
	}
}
