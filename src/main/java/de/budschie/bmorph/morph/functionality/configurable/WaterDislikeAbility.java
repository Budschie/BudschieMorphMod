package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WaterDislikeAbility extends AbstractEventAbility
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
		if(event.player.isInWaterRainOrBubbleColumn() && trackedPlayers.contains(event.player.getUniqueID()))
		{
			event.player.attackEntityFrom(DamageSource.DROWN, damageAmount);
		}
	}
}
