package de.budschie.bmorph.morph.functionality.configurable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphStateChangeEvent;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class StateMachineAbilityManager extends Ability
{
	// TODO: Add support for mixing of abilities and ability gorups
	public static class StateMachineListener 
	{
		private String stateValue;
		private LazyOptional<List<Ability>> abilities;
		
		public StateMachineListener(String stateValue, LazyOptional<List<Ability>> abilities)
		{
			this.stateValue = stateValue;
			this.abilities = abilities;
		}
		
		public String getStateValue()
		{
			return stateValue;
		}
		
		public LazyOptional<List<Ability>> getAbilities()
		{
			return abilities;
		}
	}
	
	public static final Codec<StateMachineListener> STATE_MACHINE_LISTENER = RecordCodecBuilder.create(instance ->
		instance.group(
				Codec.STRING.fieldOf("state_value").forGetter(StateMachineListener::getStateValue), 
				ModCodecs.ABILITY_LIST.fieldOf("abilities").forGetter(StateMachineListener::getAbilities))
		.apply(instance, StateMachineListener::new)
	);
	
	public static final Codec<StateMachineAbilityManager> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("state_key").forGetter(StateMachineAbilityManager::getStateKey),
					STATE_MACHINE_LISTENER.listOf().optionalFieldOf("state_listeners", Arrays.asList()).forGetter(StateMachineAbilityManager::getStateListeners),
					ModCodecs.ABILITY_LIST.optionalFieldOf("default", LazyOptional.of(() -> Arrays.asList())).forGetter(StateMachineAbilityManager::getDefaultCase),
					Codec.STRING.optionalFieldOf("reset_value").forGetter(StateMachineAbilityManager::getResetValue))
			.apply(instance, StateMachineAbilityManager::new));
	
	private ResourceLocation stateKey;
	private List<StateMachineListener> stateListeners;
	private LazyOptional<List<Ability>> defaultCase;
	private HashMap<UUID, List<ResourceLocation>> currentlyAppliedAbilities = new HashMap<>();
	private Optional<String> resetValue;
	
	public StateMachineAbilityManager(ResourceLocation stateKey, List<StateMachineListener> stateListeners, LazyOptional<List<Ability>> defaultCase, Optional<String> resetValue)
	{
		this.stateKey = stateKey;
		this.stateListeners = stateListeners;
		this.defaultCase = defaultCase;
		this.resetValue = resetValue;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{	
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		currentlyAppliedAbilities.put(player.getUUID(), new ArrayList<>());
		// enableAssociatedAbilities(player, defaultCase);
		
		IMorphCapability cap = MorphUtil.getCapOrNull(player);
		
		if(resetValue.isPresent())
		{
//			cap.createMorphStateMachineChangeRecorder().recordChange(stateKey, new MorphStateMachineEntry(Optional.of(new TickTimestamp()), resetValue)).finishRecording().applyChanges();
		}
		
		handleState(player, cap.getMorphStateMachine().query(stateKey));	
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		disableAssociatedAbilities(player);
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
	}
	
	@Override
	public void removePlayerReferences(Player playerRefToRemove)
	{
		currentlyAppliedAbilities.remove(playerRefToRemove.getUUID());
		super.removePlayerReferences(playerRefToRemove);
	}
	
	@Override
	public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
	{
		super.serialize(player, context, canSaveTransientData);
		
		if(canSaveTransientData)
		{
			CompoundTag transientTag = context.getOrCreateSerializationObjectForAbility(this).getOrCreateTransientTag();
			ListTag list = new ListTag();
			transientTag.put("temporary_abilities", list);
			
			for(ResourceLocation ability : currentlyAppliedAbilities.get(player.getUUID()))
			{
				list.add(StringTag.valueOf(ability.toString()));
			}
		}
	}
	
	@Override
	public void deserialize(Player player, AbilitySerializationContext context)
	{
		super.deserialize(player, context);
		
		ArrayList<ResourceLocation> temporaryAbilities = new ArrayList<>();
		currentlyAppliedAbilities.put(player.getUUID(), temporaryAbilities);
		
		context.getOrCreateSerializationObjectForAbility(this).getTransientTag().ifPresent(transientTag ->
		{
			ListTag list = transientTag.getList("temporary_abilities", Tag.TAG_STRING);
			
			for(int i = 0; i < list.size(); i++)
			{
				temporaryAbilities.add(new ResourceLocation(list.getString(i)));
			}
		});
	}
	
	private void disableAssociatedAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			List<ResourceLocation> appliedAbilities = currentlyAppliedAbilities.get(player.getUUID());
			
			if(appliedAbilities == null)
			{
				return;
			}
			
			for(ResourceLocation resourceLocation : appliedAbilities)
			{
				cap.deapplyAbility(BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(resourceLocation));
			}
		});
		
		List<ResourceLocation> currentlyAppliedAbilitiesForPlayer = currentlyAppliedAbilities.get(player.getUUID());
		
		if(currentlyAppliedAbilitiesForPlayer != null)
		{
			currentlyAppliedAbilitiesForPlayer.clear();
		}
	}
	
	private void enableAssociatedAbilities(Player player, LazyOptional<List<Ability>> abilities)
	{
		MorphUtil.processCap(player, cap ->
		{
			List<ResourceLocation> temporaryAbilities = new ArrayList<>();
			currentlyAppliedAbilities.put(player.getUUID(), temporaryAbilities);
			
			for(Ability ability : abilities.resolve().get())
			{
				cap.applyAbility(ability);
				temporaryAbilities.add(ability.getResourceLocation());
			}
		});
	}
	
	private void handleState(Player player, Optional<MorphStateMachineEntry> value)
	{
		if(value.isEmpty())
		{
			enableAssociatedAbilities(player, defaultCase);
			return;
		}
		
		if(value.get().getValue().isEmpty())
		{
			enableAssociatedAbilities(player, defaultCase);
			return;
		}
		
		for(StateMachineListener listener : this.stateListeners)
		{
			if(listener.getStateValue().equals(value.get().getValue().get()))
			{
				enableAssociatedAbilities(player, listener.getAbilities());
				return;
			}
		}
		
		enableAssociatedAbilities(player, defaultCase);
	}
	
	@SubscribeEvent
	public void onStateChanged(MorphStateChangeEvent event)
	{
		if(event.getPlayer().level.isClientSide())
		{
			return;
		}
		
		if(!isTracked(event.getPlayer()))
		{
			return;
		}
		
		if(!event.getMorphStateChange().getStateKey().equals(this.stateKey))
		{
			return;
		}
		
		// Disable old abilities
		disableAssociatedAbilities(event.getPlayer());
		
		handleState(event.getPlayer(), event.getMorphStateChange().getNewValue());
	}
	
	public LazyOptional<List<Ability>> getDefaultCase()
	{
		return defaultCase;
	}
	
	public ResourceLocation getStateKey()
	{
		return stateKey;
	}
	
	public List<StateMachineListener> getStateListeners()
	{
		return stateListeners;
	}
	
	public Optional<String> getResetValue()
	{
		return resetValue;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
