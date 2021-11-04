package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Ability that gives entities effects when a player hurts them. **/
public class EffectOnAttackEntity extends AbstractEventAbility
{
	public static final Codec<EffectOnAttackEntity> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ModCodecs.EFFECT_INSTANCE.fieldOf("effect_instance").forGetter(EffectOnAttackEntity::getEffectInstance)).apply(instance, EffectOnAttackEntity::new));
	
	private EffectInstance effectInstance;
	
	public EffectOnAttackEntity(EffectInstance effectInstance)
	{
		this.effectInstance = effectInstance;
	}
	
	public EffectInstance getEffectInstance()
	{
		return effectInstance;
	}
	
	@SubscribeEvent
	public void onEntityDamaged(LivingDamageEvent event)
	{
		if(event.getSource().getTrueSource() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
			
			if(isTracked(player))
			{
				event.getEntityLiving().addPotionEffect(new EffectInstance(this.effectInstance));
			}
		}
	}
}
