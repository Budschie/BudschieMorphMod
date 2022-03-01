package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WaterDislikeAbility extends Ability
{
	public static final Codec<WaterDislikeAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.optionalFieldOf("damage_amount", 1.0f).forGetter(WaterDislikeAbility::getDamageAmount))
			.apply(instance, WaterDislikeAbility::new));
	
	// The damage amount per tick
	private float damageAmount;
	
	public WaterDislikeAbility(float damageAmount)
	{
		this.damageAmount = damageAmount;
	}
	
	public float getDamageAmount()
	{
		return damageAmount;
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			if(event.player.isInWaterRainOrBubble() && trackedPlayers.contains(event.player.getUUID()))
			{
				event.player.hurt(DamageSource.DROWN, damageAmount);
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
