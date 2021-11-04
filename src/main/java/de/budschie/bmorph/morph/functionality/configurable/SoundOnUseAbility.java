package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;

public class SoundOnUseAbility extends StunAbility
{
	public static final Codec<SoundOnUseAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					Codec.INT.fieldOf("stun").forGetter(SoundOnUseAbility::getStun),
					SoundInstance.CODEC.fieldOf("sound").forGetter(SoundOnUseAbility::getSoundInstance))
					.apply(instance, SoundOnUseAbility::new));
	
	private SoundInstance soundInstance;
	
	public SoundOnUseAbility(int stun, SoundInstance soundInstance)
	{
		super(stun);
		this.soundInstance = soundInstance;
	}
	
	public SoundInstance getSoundInstance()
	{
		return soundInstance;
	}
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
			stun(player.getUniqueID());
			soundInstance.playSoundAt(player);
		}
	}
}
