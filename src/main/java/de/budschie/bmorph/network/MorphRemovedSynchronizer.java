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
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MorphRemovedSynchronizer implements ISimpleImplPacket<MorphRemovedPacket>
{
	@Override
	public void encode(MorphRemovedPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayerUUID());
		buffer.writeInt(packet.getRemovedMorph());
	}

	@Override
	public MorphRemovedPacket decode(FriendlyByteBuf buffer)
	{
		return new MorphRemovedPacket(buffer.readUUID(), buffer.readInt());
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
					
					resolved.removeFromMorphList(packet.getRemovedMorph());
				}
				
				MorphGuiHandler.updateMorphUi();
				ctx.get().setPacketHandled(true);
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
