package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.network.PufferfishPuff.PufferfishPuffPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class PufferfishPuff implements ISimpleImplPacket<PufferfishPuffPacket>
{
	@Override
	public void encode(PufferfishPuffPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.getOriginalDuration());
		buffer.writeInt(packet.getDuration());
		buffer.writeUUID(packet.getPlayer());
	}

	@Override
	public PufferfishPuffPacket decode(FriendlyByteBuf buffer)
	{
		return new PufferfishPuffPacket(buffer.readInt(), buffer.readInt(), buffer.readUUID());
	}

	@Override
	public void handle(PufferfishPuffPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnlyShit.handlePufferfishPacketClient(packet));
		});
	}
	
	public static class PufferfishPuffPacket
	{
		private int originalDuration;
		private int duration;
		private UUID player;
		
		public PufferfishPuffPacket(int originalDuration, int duration, UUID player)
		{
			this.originalDuration = originalDuration;
			this.duration = duration;
			this.player = player;
		}
		
		public int getDuration()
		{
			return duration;
		}
		
		public int getOriginalDuration()
		{
			return originalDuration;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
		
		public void setDuration(int duration)
		{
			this.duration = duration;
		}
		
		public void setOriginalDuration(int originalDuration)
		{
			this.originalDuration = originalDuration;
		}
		
		public void setPlayer(UUID player)
		{
			this.player = player;
		}
	}
}
