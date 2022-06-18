package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityHandler;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityInstance;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PufferfishAbility extends StunAbility
{
	public static final Codec<PufferfishAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					Codec.INT.fieldOf("stun").forGetter(PufferfishAbility::getStun),
					Codec.list(ModCodecs.EFFECT_INSTANCE).fieldOf("effects_on_use").forGetter(PufferfishAbility::getEffects),
					Codec.FLOAT.fieldOf("direct_damage").forGetter(PufferfishAbility::getDirectDamage),
					Codec.FLOAT.fieldOf("ability_radius").forGetter(PufferfishAbility::getRadius),
					Codec.INT.fieldOf("duration").forGetter(PufferfishAbility::getDuration),
					SoundInstance.CODEC.optionalFieldOf("sting_sound", new SoundInstance(SoundEvents.PUFFER_FISH_STING, SoundSource.HOSTILE, 1, .125f, 1)).forGetter(PufferfishAbility::getStingSoundEffect),
					SoundInstance.CODEC.optionalFieldOf("fish_blow_up", new SoundInstance(SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.HOSTILE, 1, .125f, 1))
							.forGetter(PufferfishAbility::getBlowUpSoundEffect),
					SoundInstance.CODEC.optionalFieldOf("fish_blow_out", new SoundInstance(SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.HOSTILE, 1, .125f, 1))
							.forGetter(PufferfishAbility::getBlowOutSoundEffect))
					.apply(instance, PufferfishAbility::new));
	
	private List<MobEffectInstance> effects;
	private float directDamage;
	private float radius;
	private int duration;
	private SoundInstance stingSoundEffect;
	private SoundInstance blowUpSoundEffect;
	private SoundInstance blowOutSoundEffect;
	
	public PufferfishAbility(int stun, List<MobEffectInstance> effects, float directDamage, float radius, int duration, SoundInstance stingSoundEffect, SoundInstance blowUpSoundEffect, SoundInstance blowOutSoundEffect)
	{
		super(stun);
		
		this.effects = effects;
		this.directDamage = directDamage;
		this.radius = radius;
		this.duration = duration;
		this.stingSoundEffect = stingSoundEffect;
		// Mojang really gives its sounds weird names... But I'm gonna adopt them nontheless
		this.blowUpSoundEffect = blowUpSoundEffect;
		this.blowOutSoundEffect = blowOutSoundEffect;
	}
	
	public SoundInstance getStingSoundEffect()
	{
		return stingSoundEffect;
	}
	
	public SoundInstance getBlowUpSoundEffect()
	{
		return blowUpSoundEffect;
	}
	
	public SoundInstance getBlowOutSoundEffect()
	{
		return blowOutSoundEffect;
	}
	
	public List<MobEffectInstance> getEffects()
	{
		return effects;
	}
	
	public float getDirectDamage()
	{
		return directDamage;
	}
	
	public float getRadius()
	{
		return radius;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	@SubscribeEvent
	public void onPlayerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			for(UUID playerId : trackedPlayers)
			{
				Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);
				
				if(trackedPlayers.contains(player.getUUID()))
				{
					player.getCapability(PufferfishCapabilityInstance.PUFFER_CAP).ifPresent(cap ->
					{
						if(cap.getPuffTime() > 0)
						{
							List<LivingEntity> entities = player.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius));
							
							for(LivingEntity entity : entities)
							{
								if(entity == player || !entity.isAlive())
									continue;
	
								if(entity.hurt(DamageSource.mobAttack(player), directDamage))
								{
									for(MobEffectInstance effect : effects)
									{
										entity.addEffect(new MobEffectInstance(effect));
									}
									
									stingSoundEffect.playSoundAt(entity);
								}
							}
							
							if(cap.getPuffTime() == 5)
							{
								blowOutSoundEffect.playSoundAt(player);
							}	
						}
					});
				}
			}
		}
	}
		
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{		
		if(!isCurrentlyStunned(player.getUUID()))
		{
			stun(player.getUUID());
			
			PufferfishCapabilityHandler.INSTANCE.puffServer(player, duration);
			blowUpSoundEffect.playSoundAt(player);
		}
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		player.getCapability(PufferfishCapabilityInstance.PUFFER_CAP).ifPresent(cap ->
		{
			cap.puff(0);
		});
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
