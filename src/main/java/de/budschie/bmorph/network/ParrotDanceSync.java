package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer;
import de.budschie.bmorph.capabilities.parrot_dance.IParrotDanceCapability;
import de.budschie.bmorph.capabilities.parrot_dance.ParrotDanceCapabilityInstance;
import de.budschie.bmorph.network.ParrotDanceSync.ParrotDanceSyncPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class ParrotDanceSync extends CommonCapabilitySynchronizer<ParrotDanceSyncPacket, IParrotDanceCapability>
{
	public ParrotDanceSync()
	{
		super(ParrotDanceCapabilityInstance.PARROT_CAP);
	}

	@Override
	public void encodeAdditional(ParrotDanceSyncPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.isDancing());
	}

	@Override
	public ParrotDanceSyncPacket decodeAdditional(FriendlyByteBuf buffer)
	{
		return new ParrotDanceSyncPacket(buffer.readBoolean());
	}

	@Override
	public boolean handleCapabilitySync(ParrotDanceSyncPacket packet, Supplier<Context> ctx, Player player, IParrotDanceCapability capabilityInterface)
	{
		capabilityInterface.setDancing(packet.isDancing());
		return true;
	}
	
	public static class ParrotDanceSyncPacket extends CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket
	{
		private boolean dancing;
		
		public ParrotDanceSyncPacket(boolean dancing)
		{
			this.dancing = dancing;
		}
		
		public boolean isDancing()
		{
			return dancing;
		}
	}
}
