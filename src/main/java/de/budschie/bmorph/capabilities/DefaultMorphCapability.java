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
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphAddedSynchronizer;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer;
import de.budschie.bmorph.network.MorphChangedSynchronizer;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

public class DefaultMorphCapability implements IMorphCapability
{
	boolean mobAttack = false;
	
	int aggroTimestamp = 0;
	int aggroDuration = 0;
	
	Optional<MorphItem> morph = Optional.empty();
	Optional<Integer> currentMorphIndex = Optional.empty();
	
	MorphList morphList = new MorphList();
	FavouriteList favouriteList = new FavouriteList(morphList);
	
	List<Ability> currentAbilities = new ArrayList<>();
	
	@Override
	public void syncWithClients(Player player)
	{
		if(player.level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), player.getUUID()));
		}
	}
	
	@Override
	public void syncWithClient(Player player, ServerPlayer syncTo)
	{
		if(player.level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTo), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), player.getUUID()));
		}
	}
	
	@Override
	public void syncWithConnection(Player player, Connection connection)
	{
		if(player.level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.NMLIST.with(() -> Lists.newArrayList(connection)), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, favouriteList, serializeAbilities(), player.getUUID()));
		}
	}
	
	@Override
	public void syncMorphChange(Player player)
	{
		if(player.level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphChangedSynchronizer.MorphChangedPacket(player.getUUID(), currentMorphIndex, morph, serializeAbilities()));
	}

	@Override
	public void syncMorphAcquisition(Player player, MorphItem item)
	{
		if(player.level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphAddedSynchronizer.MorphAddedPacket(player.getUUID(), item));
	}

	@Override
	public void syncMorphRemoval(Player player, int index)
	{
		if(player.level.isClientSide)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphRemovedPacket(player.getUUID(), index));
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
	public void addToMorphList(MorphItem morphItem)
	{
		morphList.addToMorphList(morphItem);
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
	public void applyHealthOnPlayer(Player player)
	{
		// Not really implemented yet...
		float playerHealthPercentage = player.getHealth() / player.getMaxHealth();
		
		if(!getCurrentMorph().isPresent())
		{
			player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
			player.setHealth(20f * playerHealthPercentage);
		}
		else
		{
			// xD why is this a legal identifier
			Entity thisisnotveryperformantoranythinglikethisbutidontcarealsothisnameisverystupidsoidkmaybeishouldhchangethislaterbutontheotherhandthisisalsobtwyouhavejustfoundaneasteregginmycode = getCurrentMorph().get().createEntity(player.level);
			
			if(thisisnotveryperformantoranythinglikethisbutidontcarealsothisnameisverystupidsoidkmaybeishouldhchangethislaterbutontheotherhandthisisalsobtwyouhavejustfoundaneasteregginmycode instanceof LivingEntity)
			{
				float maxHealthOfEntity = ((LivingEntity)thisisnotveryperformantoranythinglikethisbutidontcarealsothisnameisverystupidsoidkmaybeishouldhchangethislaterbutontheotherhandthisisalsobtwyouhavejustfoundaneasteregginmycode).getMaxHealth();
				player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealthOfEntity);
				player.setHealth(maxHealthOfEntity * playerHealthPercentage);
			}
			else
			{
				// This is some bad copy pasta right here, which is f*cking bad, but i dont wanna think right now
				player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
				player.setHealth(20f * playerHealthPercentage);
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
		return currentAbilities;
	}

	@Override
	public void setCurrentAbilities(List<Ability> abilities)
	{
		this.currentAbilities = abilities;
	}

	@Override
	public void applyAbilities(Player player)
	{
		if(getCurrentAbilities() != null && getCurrentMorph().isPresent())
			getCurrentAbilities().forEach(ability -> ability.enableAbility(player, getCurrentMorph().get()));
	}

	@Override
	public void deapplyAbilities(Player player)
	{
		if(getCurrentAbilities() != null)
			getCurrentAbilities().forEach(ability -> ability.disableAbility(player, getCurrentMorph().get()));
	}

	@Override
	public void useAbility(Player player)
	{
		if(getCurrentAbilities() != null)
			getCurrentAbilities().forEach(ability -> ability.onUsedAbility(player, getCurrentMorph().get()));
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

	@Override
	public void applyAbility(Player player, Ability ability)
	{
		if(this.getCurrentAbilities() == null)
			currentAbilities = Arrays.asList(ability);
		else
			currentAbilities.add(ability);
		
		ability.enableAbility(player, getCurrentMorph().orElse(null));
	}

	@Override
	public void deapplyAbility(Player player, Ability ability)
	{
		if(this.getCurrentAbilities() != null)
		{
			currentAbilities.remove(ability);
			
			ability.disableAbility(player, getCurrentMorph().orElse(null));
		}
	}
}
