package de.budschie.bmorph.morph.functionality.configurable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.StunAbility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PredicateAbility extends StunAbility
{
	public PredicateAbility(int stun)
	{
		super(stun);
	}

	// Predicate that the player should be tested against
	private LazyRegistryWrapper<LootItemCondition> predicate;
	
	// Tells us whether we want this ability to execute only once per lifetime
	private boolean executeOnce;
	
	// Tells us how long this predicate shall be true before the given ability is being executed
	private int predicateTrueForTime;
	
	// Abiltiy that shall be executed when the predicate is true for the given amount of time
	private LazyOptional<Ability> abilityToExecuteOnSuccess; 
	
	// List of timestamps and players that are locked
	private HashMap<UUID, Integer> players = new HashMap<>();
	private HashSet<UUID> alreadyReceivedAbility = new HashSet<>();
	
	@SubscribeEvent
	public void onPlayersUpdated(ServerTickEvent event)
	{
		LootItemCondition predicateResolved = predicate.getWrappedType();
		
		if(predicateResolved == null)
			return;
		
		playerIteration:
		for(UUID uuid : trackedPlayers)
		{
			if(alreadyReceivedAbility.contains(uuid) || isCurrentlyStunned(uuid))
				continue playerIteration;
			
			Player currentPlayer = ServerSetup.server.getPlayerList().getPlayer(uuid);
			
			// First, check if the predicate is true
			
			
			LootContext.Builder prediacteContext = (new LootContext.Builder((ServerLevel)currentPlayer.level)).withParameter(LootContextParams.ORIGIN, currentPlayer.position())
					.withOptionalParameter(LootContextParams.THIS_ENTITY, currentPlayer);
			
			boolean predicateTrue = predicateResolved.test(prediacteContext.create(LootContextParamSets.COMMAND));
			
			if(predicateTrue)
			{
				if(players.containsKey(currentPlayer.getUUID()))
				{
					// Test if we shall apply the ability
					if(hasPassedTimestamp(currentPlayer))
					{
						removePlayerFromTimestamp(currentPlayer);
						
						if(executeOnce)
							alreadyReceivedAbility.add(uuid);
						
						abilityToExecuteOnSuccess.resolve().get().onUsedAbility(currentPlayer, MorphUtil.getCapOrNull(currentPlayer).getCurrentMorph().get());
					}
				}
				else
				{
					addTimestampForPlayer(currentPlayer, predicateTrueForTime);
				}
			}
			else
			{
				if(players.containsKey(currentPlayer.getUUID()))
				{
					removePlayerFromTimestamp(currentPlayer);
				}
			}
		}
	}
	
	@Override
	public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
	{
		super.serialize(player, context, canSaveTransientData);
		
		CompoundTag tag = context.getOrCreateSerializationObjectForAbility(this).getOrCreatePersistentTag();
		
		if(players.containsKey(player.getUUID()))
		{
			tag.putInt("predicate_ability_time_until_ability", getTimeLeftForPlayerToReceiveAbility(player));
		}
		
		if(alreadyReceivedAbility.contains(player.getUUID()))
		{
			tag.putBoolean("predicate_ability_already_received", true);
		}
	}
	
	@Override
	public void deserialize(Player player, AbilitySerializationContext context)
	{
		super.deserialize(player, context);
		
		context.getSerializationObjectForAbility(this).ifPresent(obj -> obj.getPersistentTag().ifPresent(tag ->
		{
			if(tag.contains("predicate_ability_time_until_ability"))
			{
				addTimestampForPlayer(player, tag.getInt("predicate_ability_time_until_ability"));
			}
			
			if(tag.contains("predicate_ability_already_received") && executeOnce)
			{
				alreadyReceivedAbility.add(player.getUUID());
			}
		}));		
	}
	
	public int getTimeLeftForPlayerToReceiveAbility(Player player)
	{
		return players.get(player.getUUID()) - ServerSetup.server.getTickCount();
	}
	
	public void removePlayerFromTimestamp(Player player)
	{
		players.remove(player.getUUID());
	}
	
	public void addTimestampForPlayer(Player player, int time)
	{
		players.put(player.getUUID(), time + ServerSetup.server.getTickCount());
	}
	
	public boolean hasPassedTimestamp(Player player)
	{
		return players.containsKey(player.getUUID()) && ServerSetup.server.getTickCount() > players.get(player.getUUID());
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		players.remove(player.getUUID());
		alreadyReceivedAbility.remove(player.getUUID());
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
