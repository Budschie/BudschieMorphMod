package de.budschie.bmorph.capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.Ability.AbilityChangeReason;
import de.budschie.bmorph.network.AdditionalAbilitySynchronization;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphAddedSynchronizer;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer;
import de.budschie.bmorph.network.MorphChangedSynchronizer;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import de.budschie.bmorph.util.LockableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class DefaultMorphCapability implements IMorphCapability
{
	Player owner;
	
	boolean mobAttack = false;
	
	int aggroTimestamp = 0;
	int aggroDuration = 0;
	
	Optional<MorphItem> morph = Optional.empty();
	Optional<Integer> currentMorphIndex = Optional.empty();
	
	MorphList morphList = new MorphList();
	FavouriteList favouriteList = new FavouriteList(morphList);
	
	LockableList<Ability> currentAbilities = new LockableList<>();
	
	AbilitySerializationContext context = new AbilitySerializationContext();
	
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
	}
	
	@Override
	public Player getOwner()
	{
		return owner;
	}
	
	@Override
	public void syncWithClients()
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> getOwner()), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), getOwner().getUUID()));
		}
	}
	
	@Override
	public void syncWithClient(ServerPlayer syncTo)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTo), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), getOwner().getUUID()));
		}
	}
	
	@Override
	public void syncWithConnection(Connection connection)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.NMLIST.with(() -> Lists.newArrayList(connection)), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), getOwner().getUUID()));
		}
	}
	
	@Override
	public void syncMorphChange()
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphChangedSynchronizer.MorphChangedPacket(getOwner().getUUID(), currentMorphIndex, morph, serializeAbilities()));
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
	public void syncMorphRemoval(int index)
	{
		if(getOwner().level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)getOwner()), new MorphRemovedPacket(getOwner().getUUID(), index));
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
	public int addToMorphList(MorphItem morphItem)
	{
		return morphList.addToMorphList(morphItem);
	}

	@Override
	public void removeFromMorphList(int index)
	{
		morphList.removeFromMorphList(index);
	}
	
	@Override
	public void setMorphList(MorphList list)
	{
		this.morphList = list;
		
		// Setting morph list not fully handled, but this is an edge case that never happens lulw
		this.favouriteList.setMorphList(morphList);
	}

	@Override
	/** There shall only be read access to this list, as else, changed content won't be sent to the clients. **/
	public MorphList getMorphList()
	{
		return morphList;
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
			Entity thisisnotveryperformantoranythinglikethisbutidontcarealsothisnameisverystupidsoidkmaybeishouldhchangethislaterbutontheotherhandthisisalsobtwyouhavejustfoundaneasteregginmycode = getCurrentMorph().get().createEntity(getOwner().level);
			
			if(thisisnotveryperformantoranythinglikethisbutidontcarealsothisnameisverystupidsoidkmaybeishouldhchangethislaterbutontheotherhandthisisalsobtwyouhavejustfoundaneasteregginmycode instanceof LivingEntity)
			{
				float maxHealthOfEntity = ((LivingEntity)thisisnotveryperformantoranythinglikethisbutidontcarealsothisnameisverystupidsoidkmaybeishouldhchangethislaterbutontheotherhandthisisalsobtwyouhavejustfoundaneasteregginmycode).getMaxHealth();
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
	public Optional<Integer> getCurrentMorphIndex()
	{
		return currentMorphIndex;
	}

	@Override
	public Optional<MorphItem> getCurrentMorphItem()
	{
		return morph;
	}

	@Override
	public Optional<MorphItem> getCurrentMorph()
	{
		if(currentMorphIndex.isPresent())
			return Optional.of(getMorphList().getMorphArrayList().get(currentMorphIndex.get()));
		else if(morph.isPresent())
			return morph;
		else
			return Optional.empty();
	}

	@Override
	public void setMorph(int index)
	{
		this.morph = Optional.empty();
		this.currentMorphIndex = Optional.of(index);
//		dirty = true;
	}

	@Override
	public void setMorph(MorphItem morph)
	{
		this.morph = Optional.of(morph);
		this.currentMorphIndex = Optional.empty();
//		dirty = true;
	}

	@Override
	public void demorph()
	{
		this.morph = Optional.empty();
		this.currentMorphIndex = Optional.empty();
//		dirty = true;
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
		
		if(getCurrentAbilities() != null && getCurrentMorph().isPresent())
		{
			currentAbilities.lock();
			currentAbilities.getList().forEach(ability ->
			{
				if(!getOwner().level.isClientSide())
					ability.deserialize(getOwner(), context);
				ability.enableAbility(getOwner(), getCurrentMorph().get(), oldMorphItem, oldAbilities, AbilityChangeReason.MORPHED);
			});
			currentAbilities.unlock();
		}
	}

	@Override
	public void deapplyAbilities(MorphItem aboutToMorphTo, List<Ability> newAbilities)
	{
		if(getCurrentAbilities() != null)
		{
			currentAbilities.lock();
			currentAbilities.getList().forEach(ability ->
			{
				if(!getOwner().level.isClientSide())
					ability.serialize(getOwner(), context, false);
				ability.disableAbility(getOwner(), getCurrentMorph().get(), aboutToMorphTo, newAbilities, AbilityChangeReason.MORPHED);
			});
			currentAbilities.unlock();
			
			if(!getOwner().level.isClientSide())
				context.clearTransientData();
		}
	}

	@Override
	public void useAbility()
	{
		if(getCurrentAbilities() != null)
		{
			currentAbilities.lock();
			currentAbilities.getList().forEach(ability -> ability.onUsedAbility(getOwner(), getCurrentMorph().get()));
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
			currentAbilities.safeAdd(ability);
		
		// Deserialize the ability and then enable it
		if(!getOwner().level.isClientSide())
			ability.deserialize(getOwner(), context);
		ability.enableAbility(getOwner(), getCurrentMorph().orElse(null), null, Arrays.asList(), AbilityChangeReason.DYNAMIC);
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
		}
	}

	@Override
	public CompoundTag serializeSavableAbilityData()
	{		
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
}
