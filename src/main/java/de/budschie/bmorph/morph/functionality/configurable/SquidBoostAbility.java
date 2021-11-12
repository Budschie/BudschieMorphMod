package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.SquidBoost;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class SquidBoostAbility extends StunAbility
{
	public static final Codec<SquidBoostAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("blindness_duration").forGetter(SquidBoostAbility::getBlindnessDuration),
			Codec.INT.optionalFieldOf("blindness_radius", 5).forGetter(SquidBoostAbility::getBlindnessRadius),
			Codec.FLOAT.fieldOf("boost_amount").forGetter(SquidBoostAbility::getBoostAmount),
			Codec.INT.fieldOf("stun").forGetter(SquidBoostAbility::getStun)
			).apply(instance, SquidBoostAbility::new));
	
	private Optional<Integer> blindnessDuration;
	private float boostAmount;
	private int blindnessRadius;
	
	public SquidBoostAbility(Optional<Integer> blindnessDuration, int blindnessRadius, float boostAmount, int stun)
	{
		super(stun);
		this.blindnessDuration = blindnessDuration;
		this.blindnessRadius = blindnessRadius;
		this.boostAmount = boostAmount;
	}
	
	public int getBlindnessRadius()
	{
		return blindnessRadius;
	}
	
	public Optional<Integer> getBlindnessDuration()
	{
		return blindnessDuration;
	}
	
	public float getBoostAmount()
	{
		return boostAmount;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
			//player.world.addParticle(ParticleTypes.SQUID_INK, true, player.getPosX(), player.getPosY(), player.getPosZ(), .1f, .1f, .1f);
			
			((ServerLevel)player.level).sendParticles(ParticleTypes.SQUID_INK, player.getX(), player.getY(), player.getZ(), 300, .4f, .4f, .4f, 0);
			
			if(blindnessDuration.isPresent())
			{
				player.level.getEntities(player, new AABB(player.blockPosition().offset(-blindnessRadius, -blindnessRadius, -blindnessRadius), player.blockPosition().offset(blindnessRadius, blindnessRadius, blindnessRadius))).forEach(entity ->
				{
					if(entity instanceof LivingEntity)
						((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindnessDuration.get(), 5, false, false, false));
				});
			}
			
			player.level.playSound(null, player.blockPosition(), SoundEvents.SQUID_SQUIRT, SoundSource.NEUTRAL, 10, 1);
			stun(player.getUUID());
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> player.level.dimension()), new SquidBoost.SquidBoostPacket(boostAmount, player.getUUID()));
		}		
	}
}
