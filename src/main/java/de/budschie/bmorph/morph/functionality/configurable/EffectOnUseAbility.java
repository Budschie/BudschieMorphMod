package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class EffectOnUseAbility extends StunAbility
{
	public static final Codec<EffectOnUseAbility> CODEC = RecordCodecBuilder
			.<EffectOnUseAbility>create(instance -> instance
					.group(Codec.INT.fieldOf("stun").forGetter(EffectOnUseAbility::getStun),
							ModCodecs.EFFECT_INSTANCE.fieldOf("effect_instance").forGetter(EffectOnUseAbility::getEffectInstance),
							AudioVisualEffect.CODEC.optionalFieldOf("audio_visual_effect").forGetter(EffectOnUseAbility::getAudioVisualEffect))
					.apply(instance, EffectOnUseAbility::new));
	
	// This is the variable controlling how often the effect shall be given to the player.
	private MobEffectInstance effectInstance;
	private Optional<AudioVisualEffect> audioVisualEffect;
	
	public EffectOnUseAbility(int stun, MobEffectInstance effectInstance, Optional<AudioVisualEffect> audioVisualEffect)
	{
		super(stun);
		// I am not sure how performant this is...
		this.effectInstance = effectInstance;
		this.audioVisualEffect = audioVisualEffect;
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		super.onUsedAbility(player, currentMorph);
		
		if(!isCurrentlyStunned(player.getUUID()))
		{
			stun(player.getUUID());
			player.addEffect(new MobEffectInstance(effectInstance));
			
			audioVisualEffect.ifPresent(ave -> ave.playEffect(player));
		}
	}
	
	public MobEffectInstance getEffectInstance()
	{
		return effectInstance;
	}
	
	public Optional<AudioVisualEffect> getAudioVisualEffect()
	{
		return audioVisualEffect;
	}
}
