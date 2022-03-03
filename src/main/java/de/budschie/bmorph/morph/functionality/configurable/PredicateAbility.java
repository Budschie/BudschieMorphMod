package de.budschie.bmorph.morph.functionality.configurable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
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
	public static final Codec<PredicateAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.INT.fieldOf("stun").forGetter(PredicateAbility::getStun), 
					ModCodecs.PREDICATE.listOf().listOf().fieldOf("predicates").forGetter(PredicateAbility::getPredicates),
					Codec.BOOL.fieldOf("execute_once").forGetter(PredicateAbility::shouldExecuteOnce),
					Codec.INT.fieldOf("predicate_true_for_time").forGetter(PredicateAbility::getPredicateTrueForTime),
					ModCodecs.ABILITY.fieldOf("ability_to_execute").forGetter(PredicateAbility::getAbilityToExecuteOnSuccess))
			.apply(instance, PredicateAbility::new));

	public PredicateAbility(int stun, List<List<LazyRegistryWrapper<LootItemCondition>>> predicates, boolean executeOnce, int predicateTrueForTime,
			LazyOptional<Ability> abilityToExecuteOnSuccess)
	{
		super(stun);
		this.predicates = predicates;
		this.executeOnce = executeOnce;
		this.predicateTrueForTime = predicateTrueForTime;
		this.abilityToExecuteOnSuccess = abilityToExecuteOnSuccess;
	}

	// Predicate that the player should be tested against
	// Inner list OR outer list AND (like with advancements)
	private List<List<LazyRegistryWrapper<LootItemCondition>>> predicates;
	
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
		LootItemCondition[][] lootItemConditions = new LootItemCondition[predicates.size()][];
		
		for(int i = 0; i < predicates.size(); i++)
		{
			List<LazyRegistryWrapper<LootItemCondition>> innerList = predicates.get(i);
			lootItemConditions[i] = new LootItemCondition[innerList.size()];
			
			for(int j = 0; j < innerList.size(); j++)
			{
				LootItemCondition resolved = innerList.get(j).getWrappedType();
				
				if(resolved == null)
					return;
				
				lootItemConditions[i][j] = resolved;
			}
		}
		
		playerIteration:
		for(UUID uuid : trackedPlayers)
		{
			if(alreadyReceivedAbility.contains(uuid) || isCurrentlyStunned(uuid))
				continue playerIteration;
			
			Player currentPlayer = ServerSetup.server.getPlayerList().getPlayer(uuid);
			
			// First, check if the predicate is true
			
			
			LootContext.Builder predicateContext = (new LootContext.Builder((ServerLevel)currentPlayer.level)).withParameter(LootContextParams.ORIGIN, currentPlayer.position())
					.withOptionalParameter(LootContextParams.THIS_ENTITY, currentPlayer);
			
			boolean predicateTrue = true;
			
			iterateOverList:
			for(LootItemCondition[] innerList : lootItemConditions)
			{
				for(LootItemCondition innerIteration : innerList)
				{
					// If we meet at least one predicate that is true, we can skip evaluating the others and go straight to the next set of predicates
					if(innerIteration.test(predicateContext.create(LootContextParamSets.COMMAND)))
							continue iterateOverList;
				}
				
				// If we did not go to the next list of predicates, this means that there was no predicate true. This means that AND will return false => we set "predicateTrue" to false and break out of the loop.
				predicateTrue = false;
				break iterateOverList;
			}
			
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
						
						stun(uuid);
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

	public List<List<LazyRegistryWrapper<LootItemCondition>>> getPredicates()
	{
		return predicates;
	}

	public boolean shouldExecuteOnce()
	{
		return executeOnce;
	}

	public int getPredicateTrueForTime()
	{
		return predicateTrueForTime;
	}

	public LazyOptional<Ability> getAbilityToExecuteOnSuccess()
	{
		return abilityToExecuteOnSuccess;
	}

	public HashMap<UUID, Integer> getPlayers()
	{
		return players;
	}

	public HashSet<UUID> getAlreadyReceivedAbility()
	{
		return alreadyReceivedAbility;
	}
}