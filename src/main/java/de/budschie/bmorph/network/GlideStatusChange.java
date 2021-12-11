package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.phantom_glide.ChargeDirection;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityAttacher;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatus;
import de.budschie.bmorph.network.GlideStatusChange.GlideStatusChangePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class GlideStatusChange implements ISimpleImplPacket<GlideStatusChangePacket>
{
	@Override
	public void encode(GlideStatusChangePacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayer());
		buffer.writeUtf(packet.getGlideStatus().name());
		buffer.writeInt(packet.getChargeTime());
		buffer.writeInt(packet.getMaxChargeTime());
		buffer.writeInt(packet.getTransitionTime());
		
		if(packet.getChargeDirection() != null)
		{
			buffer.writeBoolean(true);
			
			buffer.writeUtf(packet.getChargeDirection().name());
		}
		else
			buffer.writeBoolean(false);
	}

	@Override
	public GlideStatusChangePacket decode(FriendlyByteBuf buffer)
	{
		UUID player = buffer.readUUID();
		String glideStatus = buffer.readUtf();
		int chargeTime = buffer.readInt();
		int maxChargeTime = buffer.readInt();
		int transitionTime = buffer.readInt();
		
		ChargeDirection chargeDir = null;
		
		if(buffer.readBoolean())
			chargeDir = ChargeDirection.valueOf(buffer.readUtf());
		
		return new GlideStatusChangePacket(player, GlideStatus.valueOf(glideStatus), chargeTime, maxChargeTime, transitionTime, chargeDir);
	}

	@Override
	public void handle(GlideStatusChangePacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().level != null)
			{
				Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
		
				if (player != null)
				{
					player.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
					{
						cap.setGlideStatus(packet.getGlideStatus(), player);
						cap.setChargeTime(packet.getChargeTime());
						cap.setMaxChargeTime(packet.getMaxChargeTime());
						cap.setTransitionTime(packet.getTransitionTime());
						// Transition time is guaranteed to be maxtransitiontime.
						cap.setMaxTransitionTime(packet.getTransitionTime());
						cap.setChargeDirection(packet.getChargeDirection());
					});
				}
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class GlideStatusChangePacket
	{
		private UUID player;
		private GlideStatus glideStatus;
		private int chargeTime;
		private int maxChargeTime;
		private int transitionTime;
		private ChargeDirection chargeDirection;
		
		public GlideStatusChangePacket(UUID player, GlideStatus glideStatus, int chargeTime, int maxChargeTime, int transitionTime, ChargeDirection chargeDirection)
		{
			this.player = player;
			this.chargeTime = chargeTime;
			this.maxChargeTime = maxChargeTime;
			this.transitionTime = transitionTime;
			this.glideStatus = glideStatus;
			this.chargeDirection = chargeDirection;
		}

		public UUID getPlayer()
		{
			return player;
		}

		public int getChargeTime()
		{
			return chargeTime;
		}
		
		public int getMaxChargeTime()
		{
			return maxChargeTime;
		}

		public GlideStatus getGlideStatus()
		{
			return glideStatus;
		}
		
		public ChargeDirection getChargeDirection()
		{
			return chargeDirection;
		}
		
		public int getTransitionTime()
		{
			return transitionTime;
		}
	}
}
