package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer;
import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket;
import de.budschie.bmorph.capabilities.phantom_glide.ChargeDirection;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityInstance;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatus;
import de.budschie.bmorph.capabilities.phantom_glide.IGlideCapability;
import de.budschie.bmorph.network.GlideStatusChange.GlideStatusChangePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class GlideStatusChange extends CommonCapabilitySynchronizer<GlideStatusChangePacket, IGlideCapability>
{
	public GlideStatusChange()
	{
		super(GlideCapabilityInstance.GLIDE_CAP);
	}
	
	@Override
	public void encodeAdditional(GlideStatusChangePacket packet, FriendlyByteBuf buffer)
	{
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
	public GlideStatusChangePacket decodeAdditional(FriendlyByteBuf buffer)
	{
		String glideStatus = buffer.readUtf();
		int chargeTime = buffer.readInt();
		int maxChargeTime = buffer.readInt();
		int transitionTime = buffer.readInt();
		
		ChargeDirection chargeDir = null;
		
		if(buffer.readBoolean())
			chargeDir = ChargeDirection.valueOf(buffer.readUtf());
		
		return new GlideStatusChangePacket(GlideStatus.valueOf(glideStatus), chargeTime, maxChargeTime, transitionTime, chargeDir);
	}

	@Override
	public boolean handleCapabilitySync(GlideStatusChangePacket packet, Supplier<Context> ctx, Player player, IGlideCapability capabilityInterface)
	{
		capabilityInterface.setGlideStatus(packet.getGlideStatus(), player);
		capabilityInterface.setChargeTime(packet.getChargeTime());
		capabilityInterface.setMaxChargeTime(packet.getMaxChargeTime());
		capabilityInterface.setTransitionTime(packet.getTransitionTime());
		// Transition time is guaranteed to be maxtransitiontime.
		capabilityInterface.setMaxTransitionTime(packet.getTransitionTime());
		capabilityInterface.setChargeDirection(packet.getChargeDirection());
		
		return true;
	}
	
	public static class GlideStatusChangePacket extends CommonCapabilitySynchronizerPacket
	{
		private GlideStatus glideStatus;
		private int chargeTime;
		private int maxChargeTime;
		private int transitionTime;
		private ChargeDirection chargeDirection;
		
		public GlideStatusChangePacket(GlideStatus glideStatus, int chargeTime, int maxChargeTime, int transitionTime, ChargeDirection chargeDirection)
		{
			this.chargeTime = chargeTime;
			this.maxChargeTime = maxChargeTime;
			this.transitionTime = transitionTime;
			this.glideStatus = glideStatus;
			this.chargeDirection = chargeDirection;
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
