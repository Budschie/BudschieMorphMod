package de.budschie.bmorph.morph.functionality.codec_addition;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.util.ParticleCloudInstance;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This is a class that represents a bunch of audio and visual effects that can
 * be played when a morph uses its ability.
 **/
public class AudioVisualEffect
{
	public static final Codec<AudioVisualEffect> CODEC = RecordCodecBuilder.create(
			instance ->	instance.group(
				SoundInstance.CODEC.listOf().optionalFieldOf("sounds", Lists.newArrayList()).forGetter(AudioVisualEffect::getSounds),
				ParticleCloudInstance.CODEC.listOf().optionalFieldOf("particles", Lists.newArrayList()).forGetter(AudioVisualEffect::getParticleClouds))
				.apply(instance, AudioVisualEffect::new)
	);
	
	private List<SoundInstance> sounds;
	private List<ParticleCloudInstance> particleClouds;
	
	// Yeah I'm too lazy for a builder class lol
	public AudioVisualEffect(List<SoundInstance> sounds, List<ParticleCloudInstance> particleClouds)
	{
		this.sounds = sounds;
		this.particleClouds = particleClouds;
	}
	
	public AudioVisualEffect()
	{
		this(Arrays.asList(), Arrays.asList());
	}
	
	public List<ParticleCloudInstance> getParticleClouds()
	{
		return particleClouds;
	}
	
	public List<SoundInstance> getSounds()
	{
		return sounds;
	}
	
	/** Plays this effect on the client. **/
	public void playEffectClient(Level level, Vec3 at)
	{
		for(SoundInstance sound : sounds)
		{
			sound.playSound(at.x, at.y, at.z, level);
		}
		
		for(ParticleCloudInstance pCloud : particleClouds)
			pCloud.placeParticleCloudOnClient(at);
	}
	
	/** Plays this effect on the server. **/
	public void playEffectServer(ServerLevel level, Vec3 at)
	{
		for(SoundInstance sound : sounds)
		{
			sound.playSound(at.x, at.y, at.z, level);
		}
		
		for(ParticleCloudInstance pCloud : particleClouds)
			pCloud.placeParticleCloudOnServer(level, at);
	}
	
	/**
	 * Decides the method it will call. It will call either
	 * {@link AudioVisualEffect#playEffectServer} or
	 * {@link AudioVisualEffect#playEffectClient(Level, Vec3)},
	 * depending on the type of level.
	 * 
	 * If none of those levels match, it will throw an UnsupportedOperationException.
	 * 
	 * @param level Should be either a ClientLevel or a ServerLevel (or their derivatives).
	 * @param at Position where this should be played.
	 **/
	public void playEffect(Level level, Vec3 at)
	{
//		boolean serverLevel
//		
//		if(level instanceof ServerLevel sLevel)
//			playEffectServer(sLevel, at);
//		else if(level instanceof ClientLevel cLevel)
//			playEffectClient(cLevel, at);
//		else
//			throw new UnsupportedOperationException("You can't play this effect on an unknown world type. How did you even get this error TBH?!???");
		
		if(level.isClientSide())
			playEffectClient(level, at);
		else
			playEffectServer((ServerLevel)level, at);
	}
	
	/**
	 * Just like {@link AudioVisualEffect#playEffect(Level, Vec3)}, except that the
	 * level is the level of the entity and the position vec is the position of the
	 * entity.
	 **/
	public void playEffect(Entity at)
	{
		playEffect(at.level, at.position());
	}
}
