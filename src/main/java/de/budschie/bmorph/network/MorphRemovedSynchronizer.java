package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.gui.MorphGuiHandler;
import de.budschie.bmorph.network.MorphRemovedSynchronizer.MorphRemovedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphRemovedSynchronizer implements ISimpleImplPacket<MorphRemovedPacket>
{
	@Override
	public void encode(MorphRemovedPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayerUUID());
		buffer.writeInt(packet.getRemovedMorphKeys().length);
		
		for(UUID uuid : packet.getRemovedMorphKeys())
		{
			buffer.writeUUID(uuid);
		}
	}

	@Override
	public MorphRemovedPacket decode(FriendlyByteBuf buffer)
	{
		UUID player = buffer.readUUID();
		int arrayLength = buffer.readInt();
		
		UUID[] removedMorphKeys = new UUID[arrayLength];
		
		for(int i = 0; i < arrayLength; i++)
		{
			removedMorphKeys[i] = buffer.readUUID();
		}
		
		return new MorphRemovedPacket(player, removedMorphKeys);
	}

	@Override
	public void handle(MorphRemovedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().level != null)
			{
				LazyOptional<IMorphCapability> cap = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayerUUID()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					IMorphCapability resolved = cap.resolve().get();
					
					for(UUID removedMorphKey : packet.getRemovedMorphKeys())
					{
						resolved.getMorphList().removeMorphItem(removedMorphKey);
					}
				}
				
				MorphGuiHandler.updateMorphUi();
				ctx.get().setPacketHandled(true);
			}
		});
	}
	
	public static class MorphRemovedPacket
	{
		UUID playerUUID;
		UUID[] removedMorphKeys;
		
		public MorphRemovedPacket(UUID playerUUID, UUID... removedMorphKeys)
		{
			this.playerUUID = playerUUID;
			this.removedMorphKeys = removedMorphKeys;
		}
		
		public UUID[] getRemovedMorphKeys()
		{
			return removedMorphKeys;
		}
		
		public UUID getPlayerUUID()
		{
			return playerUUID;
		}
	}
}
