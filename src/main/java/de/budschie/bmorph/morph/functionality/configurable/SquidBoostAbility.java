package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.SquidBoost;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

public class SquidBoostAbility extends StunAbility
{
	public static final Codec<SquidBoostAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.EFFECT_INSTANCE.listOf().fieldOf("effects").forGetter(SquidBoostAbility::getEffects),
			Codec.INT.optionalFieldOf("effect_radius", 5).forGetter(SquidBoostAbility::getBlindnessRadius),
			Codec.FLOAT.fieldOf("boost_amount").forGetter(SquidBoostAbility::getBoostAmount),
			Codec.INT.fieldOf("stun").forGetter(SquidBoostAbility::getStun),
			AudioVisualEffect.CODEC.optionalFieldOf("audiovisual_effect", new AudioVisualEffect()).forGetter(SquidBoostAbility::getAudioVisualEffect)
			).apply(instance, SquidBoostAbility::new));
	
	private float boostAmount;
	private int effectRadius;
	private List<MobEffectInstance> effects;
	private AudioVisualEffect audioVisualEffect;
	
	public SquidBoostAbility(List<MobEffectInstance> effects, int blindnessRadius, float boostAmount, int stun, AudioVisualEffect particleClouds)
	{
		super(stun);
		this.effects = effects;
		this.effectRadius = blindnessRadius;
		this.boostAmount = boostAmount;
		
		this.audioVisualEffect = particleClouds;
	}
	
	public int getBlindnessRadius()
	{
		return effectRadius;
	}
	
	public List<MobEffectInstance> getEffects()
	{
		return effects;
	}
		
	public float getBoostAmount()
	{
		return boostAmount;
	}
	
	public AudioVisualEffect getAudioVisualEffect()
	{
		return audioVisualEffect;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
			//player.world.addParticle(ParticleTypes.SQUID_INK, true, player.getPosX(), player.getPosY(), player.getPosZ(), .1f, .1f, .1f);
			
			// Create particles
			// ((ServerLevel)player.level).sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY(), player.getZ(), 300, .4f, .4f, .4f, 0);
			
			if(!player.level.isClientSide())
				audioVisualEffect.playEffect(player);
			
			// Give effects to every entity in the radius
			player.level.getEntities(player, new AABB(player.blockPosition().offset(-effectRadius, -effectRadius, -effectRadius),
					player.blockPosition().offset(effectRadius, effectRadius, effectRadius))).forEach(entity ->
					{
						if (entity instanceof LivingEntity)
							effects.forEach(effect -> ((LivingEntity) entity).addEffect(new MobEffectInstance(effect)));
					});
			
			// Play sound
//			player.level.playSound(null, player.blockPosition(), SoundEvents.SQUID_SQUIRT, SoundSource.NEUTRAL, 10, 1);
			
			stun(player.getUUID());
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> player.level.dimension()), new SquidBoost.SquidBoostPacket(boostAmount, player.getUUID()));
		}		
	}
}
