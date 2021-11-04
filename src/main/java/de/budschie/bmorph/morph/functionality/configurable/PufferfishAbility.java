package de.budschie.bmorph.morph.functionality.configurable;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityAttacher;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class PufferfishAbility extends StunAbility
{
	public static final Codec<PufferfishAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					Codec.INT.fieldOf("stun").forGetter(PufferfishAbility::getStun),
					Codec.list(ModCodecs.EFFECT_INSTANCE).fieldOf("effects_on_use").forGetter(PufferfishAbility::getEffects),
					Codec.FLOAT.fieldOf("direct_damage").forGetter(PufferfishAbility::getDirectDamage),
					Codec.FLOAT.fieldOf("ability_radius").forGetter(PufferfishAbility::getRadius),
					Codec.INT.fieldOf("duration").forGetter(PufferfishAbility::getDuration),
					SoundInstance.CODEC.optionalFieldOf("sting_sound", new SoundInstance(SoundEvents.ENTITY_PUFFER_FISH_STING, SoundCategory.HOSTILE, 1, .125f, 1)).forGetter(PufferfishAbility::getStingSoundEffect),
					SoundInstance.CODEC.optionalFieldOf("fish_blow_up", new SoundInstance(SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, SoundCategory.HOSTILE, 1, .125f, 1))
							.forGetter(PufferfishAbility::getBlowUpSoundEffect),
					SoundInstance.CODEC.optionalFieldOf("fish_blow_out", new SoundInstance(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.HOSTILE, 1, .125f, 1))
							.forGetter(PufferfishAbility::getBlowOutSoundEffect))
					.apply(instance, PufferfishAbility::new));
	
	private List<EffectInstance> effects;
	private float directDamage;
	private float radius;
	private int duration;
	private SoundInstance stingSoundEffect;
	private SoundInstance blowUpSoundEffect;
	private SoundInstance blowOutSoundEffect;
	
	private HashSet<UUID> trackedPlayers = new HashSet<>();
	
	public PufferfishAbility(int stun, List<EffectInstance> effects, float directDamage, float radius, int duration, SoundInstance stingSoundEffect, SoundInstance blowUpSoundEffect, SoundInstance blowOutSoundEffect)
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
	
	public List<EffectInstance> getEffects()
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
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END && event.side == LogicalSide.SERVER)
		{
			if(trackedPlayers.contains(event.player.getUniqueID()))
			{
				event.player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
				{
					if(cap.getPuffTime() > 0)
					{
						List<LivingEntity> entities = event.player.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, event.player.getBoundingBox().grow(radius));
						
						for(LivingEntity entity : entities)
						{
							if(entity == event.player || !entity.isAlive())
								break;

							if(entity.attackEntityFrom(DamageSource.causeMobDamage(event.player), directDamage))
							{
								
								for(EffectInstance effect : effects)
								{
									entity.addPotionEffect(new EffectInstance(effect));
								}
								
								stingSoundEffect.playSoundAt(entity);
							}
						}
						
						if(cap.getPuffTime() == 5)
						{
							blowOutSoundEffect.playSoundAt(event.player);
						}	
					}
				});
			}
		}
	}
	
	@Override
	public void onRegister()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onUnregister()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		trackedPlayers.add(player.getUniqueID());
	}
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{		
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
			stun(player.getUniqueID());
			
			PufferfishCapabilityHandler.puffServer(player, duration);
			blowUpSoundEffect.playSoundAt(player);
		}
	}
	
	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
		{
			cap.puff(0);
		});
		trackedPlayers.remove(player.getUniqueID());
	}
}
