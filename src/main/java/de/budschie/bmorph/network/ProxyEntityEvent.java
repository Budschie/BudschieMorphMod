package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.network.ProxyEntityEvent.ProxyEntityEventPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class ProxyEntityEvent implements ISimpleImplPacket<ProxyEntityEventPacket>
{
	private Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(ProxyEntityEventPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayer());
		buffer.writeByte(packet.getEntityEventId());
	}

	@Override
	public ProxyEntityEventPacket decode(FriendlyByteBuf buffer)
	{
		return new ProxyEntityEventPacket(buffer.readUUID(), buffer.readByte());
	}

	@Override
	public void handle(ProxyEntityEventPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			// Potentially dangerous
			Player foundPlayer = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
			
			if(foundPlayer == null)
			{
				LOGGER.warn("Could not find player with the UUID {}", packet.getPlayer());
			}
			else
			{
				foundPlayer.getCapability(RenderDataCapabilityProvider.RENDER_CAP).ifPresent(cap ->
				{
					Entity cachedEntity = cap.getOrCreateCachedEntity(foundPlayer);
					
					if(cachedEntity != null)
					{
						cachedEntity.handleEntityEvent(packet.getEntityEventId());
					}
				});
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class ProxyEntityEventPacket
	{
		private UUID player;
		private byte entityEventId;
		
		public ProxyEntityEventPacket(Player player, byte entityEventId)
		{
			this(player.getUUID(), entityEventId);
		}
		
		public ProxyEntityEventPacket(UUID player, byte entityEventId)
		{
			this.player = player;
			this.entityEventId = entityEventId;
		}

		public UUID getPlayer()
		{
			return player;
		}

		public byte getEntityEventId()
		{
			return entityEventId;
		}
	}
}
