package de.budschie.bmorph.capabilities;

import java.util.Optional;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphAddedSynchronizer;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer;
import de.budschie.bmorph.network.MorphChangedSynchronizer;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class DefaultMorphCapability implements IMorphCapability
{
	Optional<MorphItem> morph = Optional.empty();
	Optional<Integer> currentMorphIndex = Optional.empty();
	
	MorphList morphList = new MorphList();
	
	private boolean dirty = true;
	
	@Override
	public void syncWithClients(PlayerEntity player)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, player.getUniqueID()));
		}
	}
	
	@Override
	public void syncWithClient(PlayerEntity player, ServerPlayerEntity syncTo)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTo), new MorphCapabilityFullSynchronizer.MorphPacket(morph, currentMorphIndex, morphList, player.getUniqueID()));
		}
	}
	
	@Override
	public void syncMorphChange(PlayerEntity player)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphChangedSynchronizer.MorphChangedPacket(player.getUniqueID(), currentMorphIndex, morph));
	}

	@Override
	public void syncMorphAcquisition(PlayerEntity player, MorphItem item)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphAddedSynchronizer.MorphAddedPacket(player.getUniqueID(), item));
	}

	@Override
	public void syncMorphRemoval(PlayerEntity player, int index)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphRemovedPacket(player.getUniqueID(), index));
	}
	
	@Override
	public void addToMorphList(MorphItem morphItem)
	{
		dirty = true;
		morphList.addToMorphList(morphItem);
	}

	@Override
	public void removeFromMorphList(int index)
	{
		dirty = true;
		morphList.removeFromMorphList(index);
	}
	
	@Override
	public void setMorphList(MorphList list)
	{
		dirty = true;
		this.morphList = list;
	}

	@Override
	/** There shall only be read access to this list, as else, changed content won't be sent to the clients. **/
	public MorphList getMorphList()
	{
		return morphList;
	}

	@Override
	public void applyHealthOnPlayer(PlayerEntity player)
	{
		// Not really implemented yet...
		float playerHealthPercentage = player.getHealth() / player.getMaxHealth();
//		player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(morph.isPresent() ? morph.get().getEntityType().get)
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
	public boolean isDirty()
	{
		return dirty;
	}

	@Override
	public void cleanDirty()
	{
		dirty = false;
	}

	@Override
	public void setMorph(int index)
	{
		this.morph = Optional.empty();
		this.currentMorphIndex = Optional.of(index);
		dirty = true;
	}

	@Override
	public void setMorph(MorphItem morph)
	{
		this.morph = Optional.of(morph);
		this.currentMorphIndex = Optional.empty();
		dirty = true;
	}

	@Override
	public void demorph()
	{
		this.morph = Optional.empty();
		this.currentMorphIndex = Optional.empty();
		dirty = true;
	}
}
