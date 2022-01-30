package de.budschie.bmorph.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SoundInstance
{
	public static final Codec<SoundSource> SOUND_CATEGORY_CODEC = Codec.STRING.flatXmap(
			str -> DataResult.success(getSoundSourceByNameOr(str, SoundSource.AMBIENT)), 
			category -> DataResult.success(category.name()));
	
	private static SoundSource getSoundSourceByNameOr(String name, SoundSource defaultSound)
	{
		SoundSource returned = SoundSource.valueOf(name);
		
		return returned == null ? defaultSound : returned;
	}
	
	public static final Codec<SoundInstance> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					ModCodecs.SOUND_EVENT_CODEC.fieldOf("sound").forGetter(SoundInstance::getSoundEvent), 
					SOUND_CATEGORY_CODEC.optionalFieldOf("category", SoundSource.AMBIENT).forGetter(SoundInstance::getSoundCategory),
					Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(SoundInstance::getPitch),
					Codec.FLOAT.optionalFieldOf("random_pitch_delta", 0.125f).forGetter(SoundInstance::getRandomPitchDelta),
					Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(SoundInstance::getVolume))
					.apply(instance, SoundInstance::new));
		
	private SoundEvent soundEvent;
	private SoundSource soundCategory;
	private float pitch;
	private float randomPitchDelta;
	private float volume;
		
	public SoundInstance(SoundEvent soundEvent, SoundSource soundCategory, float pitch, float randomPitchDelta, float volume)
	{
		this.soundEvent = soundEvent;
		this.soundCategory = soundCategory;
		this.pitch = pitch;
		this.randomPitchDelta = randomPitchDelta;
		this.volume = volume;
	}

	public SoundEvent getSoundEvent()
	{
		return soundEvent;
	}

	public SoundSource getSoundCategory()
	{
		return soundCategory;
	}

	public float getPitch()
	{
		return pitch;
	}

	public float getRandomPitchDelta()
	{
		return randomPitchDelta;
	}

	public float getVolume()
	{
		return volume;
	}

	public void playSoundAt(Entity player)
	{
		playSound(player.getX(), player.getY(), player.getZ(), player.level);
	}
	
	public void playSound(double x, double y, double z, Level world)
	{
		world.playSound(null, x, y, z, soundEvent, soundCategory, volume, getRandomPitch(world));
	}
	
	private float getRandomPitch(Level world)
	{
		return (float) ((world.getRandom().nextFloat() - 0.5) * 2 * randomPitchDelta + pitch);
	}
}
