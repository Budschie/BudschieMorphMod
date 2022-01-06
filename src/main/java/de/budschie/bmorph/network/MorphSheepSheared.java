package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer;
import de.budschie.bmorph.capabilities.common.CommonCapabilitySynchronizer.CommonCapabilitySynchronizerPacket;
import de.budschie.bmorph.capabilities.sheep.ISheepCapability;
import de.budschie.bmorph.capabilities.sheep.SheepCapabilityInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphSheepSheared extends CommonCapabilitySynchronizer<MorphSheepSheared.MorphSheepShearedPacket, ISheepCapability>
{
	public MorphSheepSheared()
	{
		super(SheepCapabilityInstance.SHEEP_CAP);
	}
	
	@Override
	public void encodeAdditional(MorphSheepShearedPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.isSheared());
	}

	@Override
	public MorphSheepShearedPacket decodeAdditional(FriendlyByteBuf buffer)
	{
		return new MorphSheepShearedPacket(buffer.readBoolean());
	}

	@Override
	public boolean handleCapabilitySync(MorphSheepShearedPacket packet, Supplier<Context> ctx, Player player, ISheepCapability capabilityInterface)
	{
		capabilityInterface.setSheared(packet.isSheared());
		
		return true;
	}

	public static class MorphSheepShearedPacket extends CommonCapabilitySynchronizerPacket
	{
		private boolean sheared;
		
		public MorphSheepShearedPacket(boolean sheared)
		{
			this.sheared = sheared;
		}
		
		public boolean isSheared()
		{
			return sheared;
		}
	}
}
