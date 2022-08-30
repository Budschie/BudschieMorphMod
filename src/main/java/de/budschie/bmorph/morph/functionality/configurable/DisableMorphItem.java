package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DisableMorphItem extends StunAbility
{
	public static final Codec<DisableMorphItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("time_until_demorph").forGetter(DisableMorphItem::getMaxMorphTime),
			Codec.INT.optionalFieldOf("disable_morph_for").forGetter(DisableMorphItem::getDisableEntityTime),
			WarningEffect.CODEC.listOf().optionalFieldOf("warning_effects", Arrays.asList()).forGetter(DisableMorphItem::getWarningEffects)
			)
			.apply(instance, DisableMorphItem::new));
	
	private Optional<Integer> disableEntityTime;
	private Optional<Integer> maxMorphTime;
	private List<WarningEffect> warningEffects;
	
	public DisableMorphItem(Optional<Integer> maxMorphTime, Optional<Integer> disableEntityTime, List<WarningEffect> warningEffects)
	{
		super(69);
		
		this.disableEntityTime = disableEntityTime;
		this.maxMorphTime = maxMorphTime;
		this.warningEffects = warningEffects;
	}
	
	public List<WarningEffect> getWarningEffects()
	{
		return warningEffects;
	}
	
	public Optional<Integer> getDisableEntityTime()
	{
		return disableEntityTime;
	}
	
	public Optional<Integer> getMaxMorphTime()
	{
		return maxMorphTime;
	}
	
	// Only execute events if we really want this entity to ummorph after a certain time
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return maxMorphTime.isPresent();
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		disableEntityTime.ifPresent(timeToDisableEntity -> 
		{ 
			enabledItem.disable(timeToDisableEntity);
		});
		
		maxMorphTime.ifPresent(timeUntilUnmorph -> stun(player.getUUID(), timeUntilUnmorph));
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event)
	{
		if(event.phase == Phase.START)
			return;
		
		UUID[] trackedPlayersClone = trackedPlayers.toArray(size -> new UUID[size]);
		
		for(UUID uuid : trackedPlayersClone)
		{
			Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
			
			int timeLeft = getStunTimeLeftFor(player);
			
			warningEffects.forEach(effect -> effect.applyWarning(player, timeLeft));
			
			if(!isCurrentlyStunned(uuid))
				MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_ABILITY.get(), player);
		}
	}
	
	public static class WarningEffect
	{
		public static Codec<WarningEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group
		(
			Codec.INT.fieldOf("ticks_left").forGetter(WarningEffect::getTicksLeft),
			ModCodecs.EFFECT_INSTANCE.listOf().optionalFieldOf("mob_effects", Arrays.asList()).forGetter(WarningEffect::getMobEffects),
			AudioVisualEffect.CODEC.optionalFieldOf("audiovisual_effect").forGetter(WarningEffect::getAudioVisualEffect)
		).apply(instance, WarningEffect::new));
		
		private int ticksLeft;
		private List<MobEffectInstance> mobEffects;
		private Optional<AudioVisualEffect> audioVisualEffect;
		
		public WarningEffect(int ticksLeft, List<MobEffectInstance> mobEffects, Optional<AudioVisualEffect> audioVisualEffect)
		{
			this.ticksLeft = ticksLeft;
			this.mobEffects = mobEffects;
			this.audioVisualEffect = audioVisualEffect;
		}
		
		public void applyWarning(Player player, int ticksLeft)
		{
			if(this.ticksLeft == ticksLeft)
			{
				mobEffects.forEach(mobEffect -> player.addEffect(new MobEffectInstance(mobEffect)));
				audioVisualEffect.ifPresent(ave -> ave.playEffect(player));
			}
		}

		public int getTicksLeft()
		{
			return ticksLeft;
		}

		public List<MobEffectInstance> getMobEffects()
		{
			return mobEffects;
		}

		public Optional<AudioVisualEffect> getAudioVisualEffect()
		{
			return audioVisualEffect;
		}
	}
}
