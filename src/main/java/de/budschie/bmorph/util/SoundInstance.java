package de.budschie.bmorph.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class SoundInstance
{
	public static final Codec<SoundCategory> SOUND_CATEGORY_CODEC = Codec.STRING.flatXmap(
			str -> DataResult.success(SoundCategory.SOUND_CATEGORIES.getOrDefault(str, SoundCategory.AMBIENT)), 
			category -> DataResult.success(category.name()));
	
	public static final Codec<SoundInstance> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					SoundEvent.CODEC.fieldOf("sound").forGetter(SoundInstance::getSoundEvent), 
					SOUND_CATEGORY_CODEC.optionalFieldOf("category", SoundCategory.AMBIENT).forGetter(SoundInstance::getSoundCategory),
					Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(SoundInstance::getPitch),
					Codec.FLOAT.optionalFieldOf("random_pitch_delta", 0.125f).forGetter(SoundInstance::getRandomPitchDelta),
					Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(SoundInstance::getVolume))
					.apply(instance, SoundInstance::new));
		
	private SoundEvent soundEvent;
	private SoundCategory soundCategory;
	private float pitch;
	private float randomPitchDelta;
	private float volume;
		
	public SoundInstance(SoundEvent soundEvent, SoundCategory soundCategory, float pitch, float randomPitchDelta, float volume)
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

	public SoundCategory getSoundCategory()
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

	public void playSoundAt(PlayerEntity player)
	{
		playSound(player.getPosX(), player.getPosY(), player.getPosZ(), player.getEntityWorld());
	}
	
	public void playSound(double x, double y, double z, World world)
	{
		world.playSound(null, x, y, z, soundEvent, soundCategory, volume, getRandomPitch(world));
	}
	
	private float getRandomPitch(World world)
	{
		return (float) ((world.getRandom().nextFloat() - 0.5) * 2 * randomPitchDelta + pitch);
	}
}
