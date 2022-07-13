package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.evoker.EvokerSpellCapabilityHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import net.minecraft.world.entity.player.Player;

public class EvokerSpellAbility extends StunAbility
{
	public static final Codec<EvokerSpellAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("stun").forGetter(EvokerSpellAbility::getStun),
			Codec.INT.fieldOf("spell_duration").forGetter(EvokerSpellAbility::getSpellDuration),
			Codec.INT.fieldOf("fangs_time_point").forGetter(EvokerSpellAbility::getSpellDuration),
			Codec.DOUBLE.fieldOf("range").forGetter(EvokerSpellAbility::getRange),
			AudioVisualEffect.CODEC.optionalFieldOf("audiovisual_effect").forGetter(EvokerSpellAbility::getAudioVisualEffect)
			).apply(instance, EvokerSpellAbility::new));
	
	private int spellDuration;
	private int fangsTimePoint;
	private double range;
	private Optional<AudioVisualEffect> audioVisualEffect;
	
	public EvokerSpellAbility(int stun, int spellDuration, int fangsTimePoint, double range, Optional<AudioVisualEffect> audioVisualEffect)
	{
		super(stun);
		
		this.spellDuration = spellDuration;
		this.range = range;
		this.audioVisualEffect = audioVisualEffect;
		this.fangsTimePoint = fangsTimePoint;
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()) && !player.level.isClientSide())
		{
			EvokerSpellCapabilityHandler.INSTANCE.useSpellServer(player, spellDuration, fangsTimePoint, range);
			audioVisualEffect.ifPresent(ave -> ave.playEffect(player));
			
			stun(player.getUUID());
		}
	}

	public int getSpellDuration()
	{
		return spellDuration;
	}

	public double getRange()
	{
		return range;
	}
	
	public int getFangsTimePoint()
	{
		return fangsTimePoint;
	}
	
	public Optional<AudioVisualEffect> getAudioVisualEffect()
	{
		return audioVisualEffect;
	}
}
