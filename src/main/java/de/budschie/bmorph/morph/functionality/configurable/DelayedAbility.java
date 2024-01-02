package de.budschie.bmorph.morph.functionality.configurable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class DelayedAbility extends Ability
{
	public static final Codec<DelayedAbility> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(Codec.INT.fieldOf("delay").forGetter(DelayedAbility::getDelay), 
				ModCodecs.ABILITY.listOf().optionalFieldOf("one_shot", Arrays.asList()).forGetter(DelayedAbility::getOneShot),
				ModCodecs.ABILITY.listOf().optionalFieldOf("enable", Arrays.asList()).forGetter(DelayedAbility::getEnable)
				).apply(instance, DelayedAbility::new)
	);
	
	// One shot and enable
	private int delay;
	private List<LazyOptional<Ability>> oneShot;
	private List<LazyOptional<Ability>> enable;
	
	public DelayedAbility(int delay, List<LazyOptional<Ability>> oneShot, List<LazyOptional<Ability>> enable)
	{
		this.delay = delay;
		this.oneShot = oneShot;
		this.enable = enable;
	}
	
	private HashMap<UUID, TickTimestamp> enabledAbilities = new HashMap<>();
	
	private void activateAbilities(Player player)
	{
		for(LazyOptional<Ability> oneShotAbility : oneShot)
		{
			oneShotAbility.resolve().get().onUsedAbility(player, MorphUtil.getCapOrNull(player).getCurrentMorph().get());
		}
		
		for(LazyOptional<Ability> enableAbility : enable)
		{
			MorphUtil.getCapOrNull(player).applyAbility(enableAbility.resolve().get());
		}
	}
	
	// FIXME: Bugs might occour under certain circumstances when changing the definition of abilities;
	// FIXME: if the enable list is changed to contain fewer elements, not all abilities will be deactivated.
	private void deactivateAbilities(Player player)
	{
		for(LazyOptional<Ability> disableAbility : enable)
		{
			MorphUtil.getCapOrNull(player).deapplyAbility(disableAbility.resolve().get());
		}
	}
	
	private boolean isAbilityDeactivationNeeded(Player player)
	{
		return this.isTracked(player) && !enabledAbilities.containsKey(player.getUUID());
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		enabledAbilities.put(player.getUUID(), new TickTimestamp(-delay));
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		if(isAbilityDeactivationNeeded(player))
		{
			deactivateAbilities(player);
		}
		
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
	}
	
	@Override
	public void removePlayerReferences(Player playerRefToRemove)
	{
		enabledAbilities.remove(playerRefToRemove.getUUID());
		super.removePlayerReferences(playerRefToRemove);
	}
	
	@SubscribeEvent
	public void onServerTickEnd(ServerTickEvent event)
	{
		if(event.phase == TickEvent.Phase.START)
		{
			return;
		}

		ArrayList<UUID> toRemove = new ArrayList<>();
		
		for(Map.Entry<UUID, TickTimestamp> playersToWatch : this.enabledAbilities.entrySet())
		{
			if(playersToWatch.getValue().getTimeElapsed() >= 0)
			{
				toRemove.add(playersToWatch.getKey());
				activateAbilities(event.getServer().getPlayerList().getPlayer(playersToWatch.getKey()));
			}
		}
		
		for(UUID uuid : toRemove)
		{
			this.enabledAbilities.remove(uuid);
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public List<LazyOptional<Ability>> getOneShot()
	{
		return oneShot;
	}
	
	public List<LazyOptional<Ability>> getEnable()
	{
		return enable;
	}
}
