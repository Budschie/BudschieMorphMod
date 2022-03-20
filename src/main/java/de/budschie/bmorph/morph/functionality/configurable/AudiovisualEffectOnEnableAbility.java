package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import net.minecraft.world.entity.player.Player;

public class AudiovisualEffectOnEnableAbility extends Ability
{
	public static final Codec<AudiovisualEffectOnEnableAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(AudioVisualEffect.CODEC.fieldOf("audiovisual_effect").forGetter(AudiovisualEffectOnEnableAbility::getAudiovisualEffect))
					.apply(instance, AudiovisualEffectOnEnableAbility::new));
	
	private AudioVisualEffect audiovisualEffect;
	
	public AudiovisualEffectOnEnableAbility(AudioVisualEffect audiovisualEffect)
	{
		this.audiovisualEffect = audiovisualEffect;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		if(!player.level.isClientSide())
			audiovisualEffect.playEffect(player);
	}
	
	public AudioVisualEffect getAudiovisualEffect()
	{
		return audiovisualEffect;
	}
}
