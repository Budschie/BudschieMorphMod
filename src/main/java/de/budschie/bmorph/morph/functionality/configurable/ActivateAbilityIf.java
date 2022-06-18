package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.capabilities.AbilitySerializationContext.AbilitySerializationObject;
import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry.AbilityGroup;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ActivateAbilityIf extends Ability
{
	public static final Codec<ActivateAbilityIf> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.either(ModCodecs.ABILITY, ModCodecs.ABILITY_GROUP).fieldOf("ability").forGetter(ActivateAbilityIf::getAbilityToActivate),
			ModCodecs.PREDICATE.listOf().listOf().optionalFieldOf("predicates", Arrays.asList()).forGetter(ActivateAbilityIf::getPredicates),
			Codec.INT.optionalFieldOf("lazy_ticks", 0).forGetter(ActivateAbilityIf::getLazyTicks),
			Codec.BOOL.optionalFieldOf("immediate_restore", false).forGetter(ActivateAbilityIf::shouldDoImmediateRestore),
			Codec.BOOL.optionalFieldOf("immediate_reset", false).forGetter(ActivateAbilityIf::shouldDoImmediateReset)
			).apply(instance, ActivateAbilityIf::new));
	
	private Either<LazyOptional<Ability>, LazyOptional<AbilityGroup>> abilityToActivate;
	private List<List<LazyRegistryWrapper<LootItemCondition>>> predicates;
	
	private HashMap<UUID, PlayerTimeHelper> playerEnableCount = new HashMap<>();
	private HashSet<UUID> playersWithAbilities = new HashSet<>();
	private int lazyTicks;
	
	// Immediate restore means that we immediately set the playerEnableCount time to max when it is non-zero and the player has their abilities and the predicates match.
	private boolean immediateRestore;
	
	// Just like immediateRestore, but inverted.
	private boolean immediateReset;
		
	public ActivateAbilityIf(Either<LazyOptional<Ability>, LazyOptional<AbilityGroup>> abilityToActivate,
			List<List<LazyRegistryWrapper<LootItemCondition>>> predicates, int lazyTicks, boolean immediateRestore, boolean immediateReset)
	{
		this.abilityToActivate = abilityToActivate;
		this.predicates = predicates;
		this.lazyTicks = lazyTicks;
		this.immediateRestore = immediateRestore;
		this.immediateReset = immediateReset;
	}

	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		if(playersWithAbilities.contains(player.getUUID()))
			disableAssociatedAbilities(player);
	}
	
	@Override
	public void removePlayerReferences(Player playerRefToRemove)
	{
		super.removePlayerReferences(playerRefToRemove);
		
		playerEnableCount.remove(playerRefToRemove.getUUID());
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.START)
			return;
		
		UUID[] clonedTrackedPlayers = trackedPlayers.toArray(size -> new UUID[size]);
		
		for(UUID playerId : clonedTrackedPlayers)
		{
			Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);
			
			LootItemCondition[][] resolved = BudschieUtils.resolveConditions(predicates);
			
			LootContext.Builder predicateContext = (new LootContext.Builder((ServerLevel)player.level)).withParameter(LootContextParams.ORIGIN, player.position())
					.withOptionalParameter(LootContextParams.THIS_ENTITY, player);
			
			if(BudschieUtils.testPredicates(resolved, () -> predicateContext.create(LootContextParamSets.COMMAND)))
			{
				incrementPlayerTicksIfPossible(player);
				
				int currentPlayerTicks = getCurrentPlayerTicks(player);
				
				if(currentPlayerTicks >= lazyTicks && !playersWithAbilities.contains(playerId))
				{
					enableAssociatedAbilities(player);
					playersWithAbilities.add(playerId);
				}
				
				if (immediateRestore && currentPlayerTicks > 0 && currentPlayerTicks < lazyTicks && playersWithAbilities.contains(player.getUUID()))
				{
					playerEnableCount.get(player.getUUID()).storedTime = lazyTicks;
				}
			}
			else
			{
				decrementPlayerTicksIfPossible(player);
				
				int currentPlayerTicks = getCurrentPlayerTicks(player);
				
				if(currentPlayerTicks <= 0 && playersWithAbilities.contains(playerId))
				{
					disableAssociatedAbilities(player);
					playersWithAbilities.remove(playerId);
				}
				
				if(immediateReset && currentPlayerTicks > 0 && !playersWithAbilities.contains(player.getUUID()))
				{
					playerEnableCount.get(player.getUUID()).storedTime = 0;
				}
			}
		}
	}
	
	@Override
	public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
	{
		super.serialize(player, context, canSaveTransientData);
		
		if(canSaveTransientData && playerEnableCount.containsKey(player.getUUID()))
			context.getOrCreateSerializationObjectForAbility(this).createTransientTag().putInt("player_time_until_activation", playerEnableCount.get(player.getUUID()).getStoredTime());
	}
	
	@Override
	public void deserialize(Player player, AbilitySerializationContext context)
	{
		super.deserialize(player, context);
		
		AbilitySerializationObject object = context.getSerializationObjectForAbilityOrNull(this);
		
		if(object != null && object.getTransientTag().isPresent() && object.getTransientTag().get().contains("player_time_until_activation"))
		{
			PlayerTimeHelper helper = new PlayerTimeHelper();
			helper.storedTime = object.getTransientTag().get().getInt("player_time_until_activation");
			playerEnableCount.put(player.getUUID(), helper);
		}
	}
	
	private void disableAssociatedAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			abilityToActivate.ifLeft(leftAbility ->
			{
				cap.deapplyAbility(leftAbility.resolve().get());
			});
			
			abilityToActivate.ifRight(rightAbilityGroup ->
			{
				rightAbilityGroup.resolve().get().getAbilities().forEach(ability -> cap.deapplyAbility(ability));
			});
		});
	}
	
	private void enableAssociatedAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			abilityToActivate.ifLeft(leftAbility ->
			{
				cap.applyAbility(leftAbility.resolve().get());
			});
			
			abilityToActivate.ifRight(rightAbilityGroup ->
			{
				rightAbilityGroup.resolve().get().getAbilities().forEach(ability -> cap.applyAbility(ability));
			});
		});
	}
	
	private void incrementPlayerTicksIfPossible(Player player)
	{
		playerEnableCount.computeIfAbsent(player.getUUID(), uuid -> new PlayerTimeHelper());
		
		playerEnableCount.get(player.getUUID()).incrementTime(lazyTicks);
	}
	
	private void decrementPlayerTicksIfPossible(Player player)
	{
		if(playerEnableCount.containsKey(player.getUUID()))
		{
			PlayerTimeHelper helper = playerEnableCount.get(player.getUUID());
			helper.decrementTime();
			
			if(helper.shouldBeDeleted())
				playerEnableCount.remove(player.getUUID());
		}
	}
	
	private int getCurrentPlayerTicks(Player player)
	{
		return playerEnableCount.containsKey(player.getUUID()) ? playerEnableCount.get(player.getUUID()).getStoredTime() : 0;
	}
	
	public Either<LazyOptional<Ability>, LazyOptional<AbilityGroup>> getAbilityToActivate()
	{
		return abilityToActivate;
	}
	
	public List<List<LazyRegistryWrapper<LootItemCondition>>> getPredicates()
	{
		return predicates;
	}
	
	public int getLazyTicks()
	{
		return lazyTicks;
	}
	
	public boolean shouldDoImmediateRestore()
	{
		return immediateRestore;
	}
	
	public boolean shouldDoImmediateReset()
	{
		return immediateReset;
	}
	
	public static class PlayerTimeHelper
	{
		private int storedTime = 0;
		
		public void incrementTime(int maxTime)
		{
			if(storedTime < maxTime)
				storedTime++;
		}
		
		public void decrementTime()
		{
			storedTime--;
		}
		
		public boolean shouldBeDeleted()
		{
			return storedTime <= 0;
		}
		
		public int getStoredTime()
		{
			return storedTime;
		}
	}
}
