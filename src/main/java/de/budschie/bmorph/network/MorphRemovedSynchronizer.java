package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MorphRemovedSynchronizer implements ISimpleImplPacket<MorphRemovedPacket>
{
	@Override
	public void encode(MorphRemovedPacket packet, PacketBuffer buffer)
	{
		buffer.writeUniqueId(packet.getPlayerUUID());
		buffer.writeInt(packet.getRemovedMorph());
	}

	@Override
	public MorphRemovedPacket decode(PacketBuffer buffer)
	{
		return new MorphRemovedPacket(buffer.readUniqueId(), buffer.readInt());
	}

	@Override
	public void handle(MorphRemovedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayerUUID()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.removeFromMorphList(packet.getRemovedMorph());
			}
		});
	}
	
	public static class MorphRemovedPacket
	{
		UUID playerUUID;
		int removedMorph;
		
		public MorphRemovedPacket(UUID playerUUID, int removedMorph)
		{
			this.playerUUID = playerUUID;
			this.removedMorph = removedMorph;
		}
		
		public int getRemovedMorph()
		{
			return removedMorph;
		}
		
		public UUID getPlayerUUID()
		{
			return playerUUID;
		}
	}
}
