package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoKnockbackAbility extends Ability
{
	public static final Codec<NoKnockbackAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(NoKnockbackAbility::getChance)).apply(instance, NoKnockbackAbility::new));
	
	private float chance;
	
	public NoKnockbackAbility(float chance)
	{
		this.chance = chance;
	}
	
	@SubscribeEvent
	public void onLivingKnockbackEvent(LivingKnockBackEvent event)
	{
		if(isTracked(event.getEntity()) && event.getEntityLiving().level.getRandom().nextFloat() < chance)
				event.setCanceled(true);
	}
	
	public float getChance()
	{
		return chance;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
