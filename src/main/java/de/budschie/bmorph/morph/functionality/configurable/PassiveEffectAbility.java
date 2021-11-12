package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.PassiveTickAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.effect.MobEffectInstance;

public class PassiveEffectAbility extends PassiveTickAbility
{
	public static final Codec<PassiveEffectAbility> CODEC = RecordCodecBuilder.<PassiveEffectAbility>create(instance -> instance
			.group(
					Codec.INT.optionalFieldOf("effect_frequency", 10).<PassiveEffectAbility>forGetter(inst -> inst.getUpdateDuration()),
					ModCodecs.EFFECT_INSTANCE.fieldOf("effect_instance").forGetter(PassiveEffectAbility::getEffectInstance)
					)
			.apply(instance, PassiveEffectAbility::new));
	
	// This is the variable controlling how often the effect shall be given to the player.
	private MobEffectInstance effectInstance;
	
	public PassiveEffectAbility(int effectFrequency, MobEffectInstance effectInstance)
	{
		// I am not sure how performant this is...
		super(effectFrequency, (player, cap) -> player.addEffect(new MobEffectInstance(effectInstance)));
		this.effectInstance = effectInstance;
	}	
	
	public MobEffectInstance getEffectInstance()
	{
		return effectInstance;
	}
}
