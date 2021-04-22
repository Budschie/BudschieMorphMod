package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.network.MorphChangedSynchronizer.MorphChangedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MorphChangedSynchronizer implements ISimpleImplPacket<MorphChangedPacket>
{
	@Override
	public void encode(MorphChangedPacket packet, PacketBuffer buffer)
	{
		buffer.writeUniqueId(packet.getPlayerUUID());
		
		buffer.writeBoolean(packet.getMorphIndex().isPresent());
		buffer.writeBoolean(packet.getMorphItem().isPresent());
		
		packet.getMorphIndex().ifPresent(index -> buffer.writeInt(index));
		packet.getMorphItem().ifPresent(item -> buffer.writeCompoundTag(item.serialize()));
	}

	@Override
	public MorphChangedPacket decode(PacketBuffer buffer)
	{
		UUID playerUUID = buffer.readUniqueId();
		boolean hasIndex = buffer.readBoolean(), hasItem = buffer.readBoolean();
		
		Optional<Integer> morphIndex = Optional.empty();
		Optional<MorphItem> morphItem = Optional.empty();
		
		if(hasIndex)
			morphIndex = Optional.of(buffer.readInt());
		
		if(hasItem)
			morphItem = Optional.of(MorphHandler.deserializeMorphItem(buffer.readCompoundTag()));
		
		return new MorphChangedPacket(playerUUID, morphIndex, morphItem);
	}

	@Override
	public void handle(MorphChangedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().world.getPlayerByUuid(packet.getPlayerUUID()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				if(packet.getMorphIndex().isPresent())
					resolved.setMorph(packet.getMorphIndex().get());
				else if(packet.getMorphItem().isPresent())
					resolved.setMorph(packet.getMorphItem().get());
				else
					resolved.demorph();
			}
		});
	}
	
	public static class MorphChangedPacket
	{
		UUID playerUUID;
		Optional<Integer> morphIndex;
		Optional<MorphItem> morphItem;
		
		public MorphChangedPacket(UUID playerUUID, Optional<Integer> morphIndex, Optional<MorphItem> morphItem)
		{
			this.playerUUID = playerUUID;
			this.morphIndex = morphIndex;
			this.morphItem = morphItem;
		}
		
		public Optional<Integer> getMorphIndex()
		{
			return morphIndex;
		}
		
		public Optional<MorphItem> getMorphItem()
		{
			return morphItem;
		}
		
		public UUID getPlayerUUID()
		{
			return playerUUID;
		}
	}
}
