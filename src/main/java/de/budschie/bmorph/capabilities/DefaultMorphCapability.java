package de.budschie.bmorph.capabilities;

import java.util.Optional;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphCapabilitySynchronizer;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class DefaultMorphCapability implements IMorphCapability
{
	Optional<MorphItem> morph = Optional.empty();
	
	MorphList morphList = new MorphList();
	
	private boolean dirty = true;
	
	@Override
	public void syncWithClients(PlayerEntity player)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new MorphCapabilitySynchronizer.MorphPacket(morph, morphList, player.getUniqueID()));
		}
	}
	
	@Override
	public void syncWithClient(PlayerEntity player, ServerPlayerEntity syncTo)
	{
		if(player.world.isRemote)
			throw new IllegalAccessError("This method may not be called on client side.");
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> syncTo), new MorphCapabilitySynchronizer.MorphPacket(morph, morphList, player.getUniqueID()));
		}
	}

	@Override
	public void setCurrentMorph(Optional<MorphItem> morph)
	{
		this.morph = morph;
		dirty = true;
	}
	
	@Override
	public void addToMorphList(MorphItem morphItem)
	{
		dirty = true;
		morphList.addToMorphList(morphItem);
	}

	@Override
	public void removeFromMorphList(MorphItem morphItem)
	{
		dirty = true;
		morphList.removeFromMorphItem(morphItem);
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
	public Optional<MorphItem> getCurrentMorph()
	{
		return morph;
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
}
