package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.PassiveTickAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.potion.EffectInstance;

public class PassiveEffectAbility extends PassiveTickAbility
{
	public static Codec<PassiveEffectAbility> CODEC = RecordCodecBuilder.<PassiveEffectAbility>create(instance -> instance
			.group(
					Codec.INT.optionalFieldOf("effect_frequency", 10).<PassiveEffectAbility>forGetter(inst -> inst.getUpdateDuration()),
					ModCodecs.EFFECT_INSTANCE.fieldOf("effect_instance").forGetter(PassiveEffectAbility::getEffectInstance)
					)
			.apply(instance, PassiveEffectAbility::new));
	
	// This is the variable controlling how often the effect shall be given to the player.
	private EffectInstance effectInstance;
	
	public PassiveEffectAbility(int effectFrequency, EffectInstance effectInstance)
	{
		super(effectFrequency, (player, cap) -> player.addPotionEffect(effectInstance));
		this.effectInstance = effectInstance;
	}	
	
	public EffectInstance getEffectInstance()
	{
		return effectInstance;
	}
}
