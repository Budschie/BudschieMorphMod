package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.gui.MorphGuiHandler;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.network.MorphAddedSynchronizer.MorphAddedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MorphAddedSynchronizer implements ISimpleImplPacket<MorphAddedPacket>
{
	@Override
	public void encode(MorphAddedPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayerUUID());
		buffer.writeNbt(packet.getAddedMorph().serialize());
	}

	@Override
	public MorphAddedPacket decode(FriendlyByteBuf buffer)
	{
		return new MorphAddedPacket(buffer.readUUID(), MorphHandler.deserializeMorphItem(buffer.readNbt()));
	}

	@Override
	public void handle(MorphAddedPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().level != null)
			{
					LazyOptional<IMorphCapability> cap = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayerUUID()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					IMorphCapability resolved = cap.resolve().get();
					
					resolved.addToMorphList(packet.getAddedMorph());
				}
				
				MorphGuiHandler.updateMorphUi();
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
