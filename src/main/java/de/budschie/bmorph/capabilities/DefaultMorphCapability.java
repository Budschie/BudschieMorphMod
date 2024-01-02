package de.budschie.bmorph.capabilities;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.Lists;

import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineChangeRecorder;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineRecordedChanges;
import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.Ability.AbilityChangeReason;
import de.budschie.bmorph.network.AdditionalAbilitySynchronization;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphAddedSynchronizer;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer;
import de.budschie.bmorph.network.MorphChangedSynchronizer;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import de.budschie.bmorph.network.MorphStateMachineChangedSync.MorphStateMachineChangedSyncPacket;
import de.budschie.bmorph.network.MorphStateMachineChangedSync.NetworkMorphStateMachineEntry;
import de.budschie.bmorph.util.LockableList;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultMorphCapability implements IMorphCapability
{
	private static final Logger LOGGER = LogManager.getLogger();

	MorphReason morphReason = MorphReasonRegistry.NONE.get();
	
	Player owner;
	
	boolean mobAttack = false;
	
	int aggroTimestamp = 0;
	int aggroDuration = 0;
	
	Optional<MorphItem> morph = Optional.empty();
	
	MorphList morphList = new MorphList();
	MorphStateMachine morphStateMachine = new MorphStateMachine();
	FavouriteList favouriteList = new FavouriteList(morphList);
	
	LockableList<Ability> currentAbilities = new LockableList<>();
	
	AbilitySerializationContext context = new AbilitySerializationContext();
	
	private Optional<Entity> cachedEntity = Optional.empty();
	
	// TODO: Merge this morph entity system with the system from the IRenderDataCapability
	@Override
	public Optional<Entity> getCurrentMorphEntity()
	{
		if(morph.isPresent())
		{
			if(!cachedEntity.isPresent())
			{
				// We do nullable instead of normal optional because an error is indicated as null here
				cachedEntity = Optional.ofNullable(morph.get().createEntity(owner.getLevel()));
			}
			
			return cachedEntity;
		}
		else
		{
			return Optional.empty();
		}
	}
	
	@Override
	public AbilitySerializationContext getAbilitySerializationContext()
	{
		return context;
	}

	@Override
	public void setAbilitySerializationContext(AbilitySerializationContext context)
	{
		this.context = context;
	}
	
	public DefaultMorphCapability(Player owner)
	{
		this.owner = owner;
		morphList.setFavouriteList(favouriteList);
	}
	
	@Override
	public Player getOwner()
	{
		return owner;
	}
	
	@Override
	public MorphStateMachineChangeRecorder createMorphStateMachineChangeRecorder()
	{
		return new MorphStateMachineChangeRecorder(owner, morphStateMachine);
	}

	@Override
	public MorphStateMachineRecordedChanges createRecordedChangesFromPacket(MorphStateMachineChangedSyncPacket packet)
	{
		// Invariant can be eliminated through fancy type magic, but I will not to that for now
		if(!packet.getPlayer().equals(this.owner.getUUID()))
		{
			throw new IllegalArgumentException("Packet contents do not refer to this player.");
		}
		
		HashMap<ResourceLocation, MorphStateMachineEntry> changes = new HashMap<>();
		
		for(Map.Entry<ResourceLocation, NetworkMorphStateMachineEntry> change : packet.getChanges().entrySet())
		{
			changes.put(change.getKey(), new MorphStateMachineEntry(change.getValue().deltaTicks().map(deltaTicks -> new TickTimestamp(deltaTicks)), change.getValue().value()));
		}
		
		return new MorphStateMachineRecordedChanges(this.owner, this.morphStateMachine, changes);
	}

	@Override
	public void syncMorphStateMachineRecordedChanges(MorphStateMachineRecordedChanges recordedChanges)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getOwner()), recordedChanges.createNetworkPacket());
		}
	}
	
	@Override
	public void syncWithClients()
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getOwner()), new MorphCapabilityFullSynchronizer.MorphPacket(morph.map(MorphItem::serialize), MorphReasonRegistry.REGISTRY.get().getKey(morphReason), morphList, morphStateMachine, favouriteList, serializeAbilities(), getOwner().getUUID()));
		}
	}
	
	@Override
	public void syncWithClient(ServerPlayer syncTo)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTo), new MorphCapabilityFullSynchronizer.MorphPacket(morph.map(MorphItem::serialize), MorphReasonRegistry.REGISTRY.get().getKey(morphReason), morphList, morphStateMachine, favouriteList, serializeAbilities(), getOwner().getUUID()));
		}
	}
	
	@Override
	public void syncWithConnection(Connection connection)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.NMLIST.with(() -> Lists.newArrayList(connection)), new MorphCapabilityFullSynchronizer.MorphPacket(morph.map(MorphItem::serialize), MorphReasonRegistry.REGISTRY.get().getKey(morphReason), morphList, morphStateMachine, favouriteList, serializeAbilities(), getOwner().getUUID()));
		}
	}
	
	@Override
	public void syncMorphChange()
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			// Other players may not have knowledge of those indices, thus we always fully send the current morph
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getOwner()), new MorphChangedSynchronizer.MorphChangedPacket(getOwner().getUUID(), getCurrentMorph(), MorphReasonRegistry.REGISTRY.get().getKey(morphReason), serializeAbilities()));
		}
	}

	@Override
	public void syncMorphAcquisition(MorphItem item)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)getOwner()), new MorphAddedSynchronizer.MorphAddedPacket(getOwner().getUUID(), item));
	}
	
	@Override
	public void syncMorphRemoval(UUID... morphItemKeys)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)getOwner()), new MorphRemovedPacket(getOwner().getUUID(), morphItemKeys));
	}
	
	@Override
	public void syncAbilityAddition(Ability... abilities)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getOwner()), new AdditionalAbilitySynchronization.AdditionalAbilitySynchronizationPacket(getOwner().getUUID(), true, abilities));
		}
	}

	@Override
	public void syncAbilityRemoval(Ability... abilities)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getOwner()), new AdditionalAbilitySynchronization.AdditionalAbilitySynchronizationPacket(getOwner().getUUID(), false, abilities));
		}
	}
	
	private ArrayList<String> serializeAbilities()
	{
		if(getCurrentAbilities() == null || getCurrentAbilities().size() == 0)
			return new ArrayList<>();
		else
		{
			ArrayList<String> toString = new ArrayList<>();
			
			for (Ability ability : getCurrentAbilities())
			{
				toString.add(ability.getResourceLocation().toString());
			}
			
			return toString;
		}
	}
		
	@Override
	public void addMorphItem(MorphItem morphItem)
	{
		morphList.addMorphItem(morphItem);
	}

	@Override
	public void removeMorphItem(MorphItem morphItem)
	{
		morphList.removeMorphItem(morphItem.getUUID());
	}

	@Override
	public void removeMorphItem(UUID key)
	{
		morphList.removeMorphItem(key);
	}
	
	@Override
	public void setMorphList(MorphList list)
	{
		this.morphList = list;
		
		// Setting morph list not fully handled, but this is an edge case that never happens lulw
		this.favouriteList.setMorphList(morphList);
		this.morphList.setFavouriteList(favouriteList);
	}

	@Override
	/** There shall only be read access to this list, as else, changed content won't be sent to the clients. **/
	public MorphList getMorphList()
	{
		return morphList;
	}
	
	@Override
	public void setMorphStateMachine(MorphStateMachine morphStateMachine)
	{
		this.morphStateMachine = morphStateMachine;
	}
	
	@Override
	public MorphStateMachine getMorphStateMachine()
	{
		return morphStateMachine;
	}

	@Override
	public void applyHealthOnPlayer()
	{
		// Not really implemented yet...
		float playerHealthPercentage = getOwner().getHealth() / getOwner().getMaxHealth();
		
		if(!getCurrentMorph().isPresent())
		{
			getOwner().getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
			getOwner().setHealth(20f * playerHealthPercentage);
		}
		else
		{
			// xD why is this a legal identifier
			Entity finallyThisHasBecomeMuchMorePerformantSinceIveImplementedAnExcellentEntityCacheWhichCanBeUsedQuiteEasily = getCurrentMorphEntity().orElse(null);
			
			if(finallyThisHasBecomeMuchMorePerformantSinceIveImplementedAnExcellentEntityCacheWhichCanBeUsedQuiteEasily instanceof LivingEntity)
			{
				float maxHealthOfEntity = ((LivingEntity)finallyThisHasBecomeMuchMorePerformantSinceIveImplementedAnExcellentEntityCacheWhichCanBeUsedQuiteEasily).getMaxHealth();
				getOwner().getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealthOfEntity);
				getOwner().setHealth(maxHealthOfEntity * playerHealthPercentage);
			}
			else
			{
				// This is some bad copy pasta right here, which is f*cking bad, but i dont wanna think right now
				getOwner().getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
				getOwner().setHealth(20f * playerHealthPercentage);
			}
		}
		
//		
	}
	
	@Override
	public Optional<MorphItem> getCurrentMorph()
	{
		if(morph.isPresent())
			return morph;
		else
			return Optional.empty();
	}

	@Override
	public void setMorph(MorphItem morph, MorphReason reason)
	{
		this.morph = Optional.of(morph);
		setMorphReason(reason);
		resetEntityCache();
	}
	
	private void resetEntityCache()
	{
		cachedEntity = Optional.empty();
	}
	
	@Override
	public void demorph(MorphReason reason)
	{
		this.morph = Optional.empty();
		setMorphReason(reason);
	}

	@Override
	public List<Ability> getCurrentAbilities()
	{
		return currentAbilities == null ? null : currentAbilities.getList();
	}

	@Override
	public void setCurrentAbilities(List<Ability> abilities)
	{
		this.currentAbilities = new LockableList<>(abilities);
	}

	@Override
	public void applyAbilities(MorphItem oldMorphItem, List<Ability> oldAbilities)
	{
		// This could be solved more efficiently, but I am not in the mood to do that, so I rely on quick, dirty and memory inefficient ways instead :kekw:
//		if(getCurrentAbilities() != null && getCurrentMorph().isPresent())
//		{
//			ArrayList<Ability> abilityCopy = new ArrayList<>();
//			abilityCopy.addAll(getCurrentAbilities());
//			abilityCopy.forEach(ability -> ability.enableAbility(player, getCurrentMorph().get()));
//		}
		
		if(getCurrentAbilities() == null || getCurrentMorph().isEmpty())
		{
			return;
		}

		currentAbilities.lock();

		for(Ability ability : currentAbilities.getList())
		{
			if (!getOwner().level.isClientSide())
			{
				ability.deserialize(getOwner(), context);
			}

			try
			{
				ability.enableAbility(getOwner(), getCurrentMorph().get(), oldMorphItem, oldAbilities, AbilityChangeReason.MORPHED);
			}
			catch(Exception exception)
			{
				LOGGER.error(MessageFormat.format("Skipped applying ability %s as there were errors loading it: %s\n%s", ability.getResourceLocation().toString(), exception.getMessage(), exception.getStackTrace()));
			}
		}

		currentAbilities.unlock();
	}

	@Override
	public void deapplyAbilities(MorphItem aboutToMorphTo, List<Ability> newAbilities)
	{
		if(getCurrentAbilities() != null && getCurrentAbilities().size() > 0)
		{
			currentAbilities.lock();
			currentAbilities.getList().forEach(ability ->
			{
				if(!getOwner().level.isClientSide())
					ability.serialize(getOwner(), context, false);
				ability.disableAbility(getOwner(), getCurrentMorph().get(), aboutToMorphTo, newAbilities, AbilityChangeReason.MORPHED);
				
			});
			currentAbilities.unlock();
			
			removePlayerReferences();
			
			if(!getOwner().level.isClientSide())
				context.clearTransientData();
			
			currentAbilities.getList().clear();
		}
	}

	@Override
	public void useAbility()
	{
		if(getCurrentAbilities() != null)
		{
			currentAbilities.lock();
			currentAbilities.getList().forEach(ability -> 
			{
				ability.onUsedAbility(getOwner(), getCurrentMorph().get());
			});
			currentAbilities.unlock();
		}
	}

	@Override
	public int getLastAggroTimestamp()
	{
		return aggroTimestamp;
	}

	@Override
	public void setLastAggroTimestamp(int timestamp)
	{
		this.aggroTimestamp = timestamp;
	}

	@Override
	public int getLastAggroDuration()
	{
		return aggroDuration;
	}

	@Override
	public void setLastAggroDuration(int aggroDuration)
	{
		this.aggroDuration = aggroDuration;
	}
	
	@Override
	public FavouriteList getFavouriteList()
	{
		return favouriteList;
	}

	@Override
	public void setFavouriteList(FavouriteList favouriteList)
	{
		this.favouriteList = favouriteList;
		this.morphList.setFavouriteList(favouriteList);
	}

	@Override
	public boolean shouldMobsAttack()
	{
		return mobAttack;
	}

	@Override
	public void setMobAttack(boolean value)
	{
		this.mobAttack = value;
	}

	/** Invokes a dynamic addition of an ability. **/
	@Override
	public void applyAbility(Ability ability)
	{
		if(this.getCurrentAbilities() == null)
			currentAbilities = new LockableList<>(Arrays.asList(ability));
		else
		{
			// May be inefficient
			if(this.getCurrentAbilities().contains(ability))
				return;
			
			currentAbilities.safeAdd(ability);
			
			// Deserialize the ability and then enable it
			if(!getOwner().level.isClientSide())
				ability.deserialize(getOwner(), context);
			ability.enableAbility(getOwner(), getCurrentMorph().orElse(null), null, Arrays.asList(), AbilityChangeReason.DYNAMIC);
		}
	}

	/** Invokes a dynamic removal of an ability. **/
	@Override
	public void deapplyAbility(Ability ability)
	{
		if(this.getCurrentAbilities() != null)
		{
			currentAbilities.safeRemove(ability);
			
			// Serialize the ability and then disable it. Transient data shall not be saved.
			if(!getOwner().level.isClientSide())
				ability.serialize(owner, context, false);
			ability.disableAbility(getOwner(), getCurrentMorph().orElse(null), null, Arrays.asList(), AbilityChangeReason.DYNAMIC);
			if(!getOwner().level.isClientSide())
				context.clearTransientDataFor(ability);
			
			ability.removePlayerReferences(owner);
		}
	}

	@Override
	public CompoundTag serializeSavableAbilityData()
	{
		if(currentAbilities == null || currentAbilities.getList() == null)
			return context.serialize();
		
		currentAbilities.lock();
		
		for(Ability ability : currentAbilities.getList())
		{
			ability.serialize(getOwner(), context, true);
		}
		
		currentAbilities.unlock();
		
		return context.serialize();
	}

	@Override
	public void deserializeSavableAbilityData(CompoundTag compoundTag)
	{
		this.context = AbilitySerializationContext.deserialize(compoundTag);
	}

	@Override
	public void removePlayerReferences()
	{
		if(this.getCurrentAbilities() != null)
		{
			this.getCurrentAbilities().forEach(ability -> ability.removePlayerReferences(owner));
		}
	}

	@Override
	public MorphReason getMorphReason()
	{
		return morphReason;
	}

	@Override
	public void setMorphReason(MorphReason reason)
	{
		this.morphReason = reason;
	}
}
