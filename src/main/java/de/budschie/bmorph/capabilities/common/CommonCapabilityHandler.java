package de.budschie.bmorph.capabilities.common;

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.network.PacketDistributor;

public abstract class CommonCapabilityHandler<C, P extends CommonCapabilitySynchronizerPacket>
{
	private Logger logger = LogManager.getLogger();
	protected Capability<C> capabilityToken;
	
	public CommonCapabilityHandler(Capability<C> capabilityToken)
	{
		this.capabilityToken = capabilityToken;
	}
	
	/** This method shall create an instance of a child of {@link CommonCapabilitySynchronizerPacket}. **/
	protected abstract P createPacket(Player player, C capability);
	
	/** This method calls {@link CommonCapabilityHandler#createPacket(Player)} and then sets the UUID for the player. **/
	public Optional<P> getPacketForPlayer(Player player)
	{
		Optional<C> resolvedCapability = player.getCapability(capabilityToken).resolve();
		
		if(resolvedCapability.isPresent())
		{
			P packet = createPacket(player, resolvedCapability.get());
			packet.setPlayer(player.getUUID());
			return Optional.of(packet);
		}
		else
		{
			logger.info(MessageFormat.format("The player {0}({1}) does not have the capability {2}.", player.getUUID(), player.getName().getString(), capabilityToken.getName()));
			return Optional.empty();
		}
	}
	
	public void synchronizeWithClient(Player toSynchronize, ServerPlayer with)
	{
		getPacketForPlayer(toSynchronize).ifPresent(packet -> MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> with), packet));
	}
	
	public void synchronizeWithClients(Player toSynchronize)
	{
		getPacketForPlayer(toSynchronize).ifPresent(packet -> MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> toSynchronize), packet));
	}
	
	public Logger getLogger()
	{
		return logger;
	}
}
