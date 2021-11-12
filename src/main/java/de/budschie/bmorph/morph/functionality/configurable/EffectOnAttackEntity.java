package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Ability that gives entities effects when a player hurts them. **/
public class EffectOnAttackEntity extends AbstractEventAbility
{
	public static final Codec<EffectOnAttackEntity> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ModCodecs.EFFECT_INSTANCE.fieldOf("effect_instance").forGetter(EffectOnAttackEntity::getEffectInstance)).apply(instance, EffectOnAttackEntity::new));
	
	private MobEffectInstance effectInstance;
	
	public EffectOnAttackEntity(MobEffectInstance effectInstance)
	{
		this.effectInstance = effectInstance;
	}
	
	public MobEffectInstance getEffectInstance()
	{
		return effectInstance;
	}
	
	@SubscribeEvent
	public void onEntityDamaged(LivingDamageEvent event)
	{
		if(event.getSource().getEntity() instanceof Player)
		{
			Player player = (Player) event.getSource().getEntity();
			
			if(isTracked(player))
			{
				event.getEntityLiving().addEffect(new MobEffectInstance(this.effectInstance));
			}
		}
	}
}
