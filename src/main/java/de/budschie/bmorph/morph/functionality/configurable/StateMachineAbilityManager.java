package de.budschie.bmorph.morph.functionality.configurable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.capabilities.MorphStateChangeEvent;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
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
		private List<LazyOptional<Ability>> abilities;
		
		public StateMachineListener(String stateValue, List<LazyOptional<Ability>> abilities)
		{
			this.stateValue = stateValue;
			this.abilities = abilities;
		}
		
		public String getStateValue()
		{
			return stateValue;
		}
		
		public List<LazyOptional<Ability>> getAbilities()
		{
			return abilities;
		}
	}
	
	public static final Codec<StateMachineListener> STATE_MACHINE_LISTENER = RecordCodecBuilder.create(instance ->
		instance.group(
				Codec.STRING.fieldOf("state_value").forGetter(StateMachineListener::getStateValue), 
				ModCodecs.ABILITY.listOf().fieldOf("abilities").forGetter(StateMachineListener::getAbilities))
		.apply(instance, StateMachineListener::new)
	);
	
	public static final Codec<StateMachineAbilityManager> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					ResourceLocation.CODEC.fieldOf("state_key").forGetter(StateMachineAbilityManager::getStateKey),
					STATE_MACHINE_LISTENER.listOf().optionalFieldOf("state_listeners", Arrays.asList()).forGetter(StateMachineAbilityManager::getStateListeners),
					ModCodecs.ABILITY.listOf().optionalFieldOf("default", Arrays.asList()).forGetter(StateMachineAbilityManager::getDefaultCase))
			.apply(instance, StateMachineAbilityManager::new));
	
	private ResourceLocation stateKey;
	private List<StateMachineListener> stateListeners;
	private List<LazyOptional<Ability>> defaultCase;
	private HashMap<UUID, List<ResourceLocation>> currentlyAppliedAbilities = new HashMap<>();
	
	public StateMachineAbilityManager(ResourceLocation stateKey, List<StateMachineListener> stateListeners, List<LazyOptional<Ability>> defaultCase)
	{
		this.stateKey = stateKey;
		this.stateListeners = stateListeners;
		this.defaultCase = defaultCase;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{	
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		currentlyAppliedAbilities.put(player.getUUID(), new ArrayList<>());
		enableAssociatedAbilities(player, defaultCase);
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
			for(ResourceLocation resourceLocation : currentlyAppliedAbilities.get(player.getUUID()))
			{
				cap.deapplyAbility(BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(resourceLocation));
			}
		});
		
		currentlyAppliedAbilities.get(player.getUUID()).clear();
	}
	
	private void enableAssociatedAbilities(Player player, List<LazyOptional<Ability>> abilities)
	{
		MorphUtil.processCap(player, cap ->
		{
			List<ResourceLocation> temporaryAbilities = currentlyAppliedAbilities.computeIfAbsent(player.getUUID(), (uuid) -> new ArrayList<>());
			
			for(LazyOptional<Ability> ability : abilities)
			{
				cap.applyAbility(ability.resolve().get());
				temporaryAbilities.add(ability.resolve().get().getResourceLocation());
			}
		});
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
		
		for(StateMachineListener listener : this.stateListeners)
		{
			if(event.getMorphStateChange().getNewValue().isEmpty())
			{
				continue;
			}
			
			if(event.getMorphStateChange().getNewValue().get().getValue().isEmpty())
			{
				continue;
			}
			
			if(listener.getStateValue().equals(event.getMorphStateChange().getNewValue().get().getValue().get()))
			{
				enableAssociatedAbilities(event.getPlayer(), listener.getAbilities());
				return;
			}
		}
		
		// FIXME: Introduces bug where timers could be reset due to reapplying the default case when switching between two unrecognized state values
		enableAssociatedAbilities(event.getPlayer(), defaultCase);
	}
	
	public List<LazyOptional<Ability>> getDefaultCase()
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
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
