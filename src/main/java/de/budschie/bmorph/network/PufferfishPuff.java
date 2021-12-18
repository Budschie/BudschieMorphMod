package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer;
import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket;
import de.budschie.bmorph.capabilities.pufferfish.IPufferfishCapability;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityInstance;
import de.budschie.bmorph.network.PufferfishPuff.PufferfishPuffPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class PufferfishPuff extends CommonCapabilitySynchronizer<PufferfishPuffPacket, IPufferfishCapability>
{
	public PufferfishPuff()
	{
		super(PufferfishCapabilityInstance.PUFFER_CAP);
	}
	
	@Override
	public void encodeAdditional(PufferfishPuffPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.getOriginalDuration());
		buffer.writeInt(packet.getDuration());
	}

	@Override
	public PufferfishPuffPacket decodeAdditional(FriendlyByteBuf buffer)
	{
		return new PufferfishPuffPacket(buffer.readInt(), buffer.readInt());
	}

	@Override
	public boolean handleCapabilitySync(PufferfishPuffPacket packet, Supplier<Context> ctx, Player player, IPufferfishCapability capabilityInterface)
	{
		capabilityInterface.setOriginalPuffTime(packet.getOriginalDuration());
		capabilityInterface.setPuffTime(packet.getDuration());
		
		return true;
	}

	
	public static class PufferfishPuffPacket extends CommonCapabilitySynchronizerPacket
	{
		private int originalDuration;
		private int duration;
		
		public PufferfishPuffPacket(int originalDuration, int duration)
		{
			this.originalDuration = originalDuration;
			this.duration = duration;
		}
		
		public int getDuration()
		{
			return duration;
		}
		
		public int getOriginalDuration()
		{
			return originalDuration;
		}
		
		public void setDuration(int duration)
		{
			this.duration = duration;
		}
		
		public void setOriginalDuration(int originalDuration)
		{
			this.originalDuration = originalDuration;
		}
	}
}
