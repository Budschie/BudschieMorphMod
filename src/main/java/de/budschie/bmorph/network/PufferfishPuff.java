package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityAttacher;
import de.budschie.bmorph.network.PufferfishPuff.PufferfishPuffPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PufferfishPuff implements ISimpleImplPacket<PufferfishPuffPacket>
{
	@Override
	public void encode(PufferfishPuffPacket packet, PacketBuffer buffer)
	{
		buffer.writeInt(packet.getDuration());
		buffer.writeUniqueId(buffer.readUniqueId());
	}

	@Override
	public PufferfishPuffPacket decode(PacketBuffer buffer)
	{
		return new PufferfishPuffPacket(buffer.readInt(), buffer.readUniqueId());
	}

	@Override
	public void handle(PufferfishPuffPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
			{
				PlayerEntity player = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayer());
				
				if(player != null)
				{
					player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
					{
						cap.puff(packet.getDuration());
					});
				}
			});
		});
	}
	
	public static class PufferfishPuffPacket
	{
		private int duration;
		private UUID player;
		
		public PufferfishPuffPacket(int duration, UUID player)
		{
			this.duration = duration;
			this.player = player;
		}
		
		public int getDuration()
		{
			return duration;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
		
		public void setDuration(int duration)
		{
			this.duration = duration;
		}
		
		public void setPlayer(UUID player)
		{
			this.player = player;
		}
	}
}
