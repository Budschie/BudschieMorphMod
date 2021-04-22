package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.network.MorphAddedSynchronizer.MorphAddedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MorphAddedSynchronizer implements ISimpleImplPacket<MorphAddedPacket>
{
	@Override
	public void encode(MorphAddedPacket packet, PacketBuffer buffer)
	{
		buffer.writeUniqueId(packet.getPlayerUUID());
		buffer.writeCompoundTag(packet.getAddedMorph().serialize());
	}

	@Override
	public MorphAddedPacket decode(PacketBuffer buffer)
	{
		return new MorphAddedPacket(buffer.readUniqueId(), MorphHandler.deserializeMorphItem(buffer.readCompoundTag()));
	}

	@Override
	public void handle(MorphAddedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayerUUID()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.addToMorphList(packet.getAddedMorph());
			}
		});
	}
	
	public static class MorphAddedPacket
	{
		UUID playerUUID;
		MorphItem addedMorph;
		
		public MorphAddedPacket(UUID playerUUID, MorphItem addedMorph)
		{
			this.playerUUID = playerUUID;
			this.addedMorph = addedMorph;
		}
		
		public MorphItem getAddedMorph()
		{
			return addedMorph;
		}
		
		public UUID getPlayerUUID()
		{
			return playerUUID;
		}
	}
}
