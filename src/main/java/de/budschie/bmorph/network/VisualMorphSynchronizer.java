package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.VisualMorphDataRegistry.VisualMorphData;
import de.budschie.bmorph.network.VisualMorphSynchronizer.VisualMorphPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class VisualMorphSynchronizer implements ISimpleImplPacket<VisualMorphPacket>
{
	@Override
	public void encode(VisualMorphPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeCollection(packet.getData(), (bufferToWriteTo, visualMorphData) ->
		{
			bufferToWriteTo.writeFloat(visualMorphData.getScale());
			bufferToWriteTo.writeResourceLocation(visualMorphData.getRegistryName());
		});
	}

	@Override
	public VisualMorphPacket decode(FriendlyByteBuf buffer)
	{
		return new VisualMorphPacket(buffer.readCollection(length -> new ArrayList<>(length), bufferToReadFrom ->
		{
			VisualMorphData data = new VisualMorphData(bufferToReadFrom.readFloat());
			data.setRegistryName(bufferToReadFrom.readResourceLocation());
			return data;
		}));
	}

	@Override
	public void handle(VisualMorphPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			BMorphMod.VISUAL_MORPH_DATA.clear();
			
			for(VisualMorphData data : packet.getData())
				BMorphMod.VISUAL_MORPH_DATA.addVisualMorphData(data);
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class VisualMorphPacket
	{
		private Collection<VisualMorphData> data;
		
		public VisualMorphPacket(Collection<VisualMorphData> data)
		{
			this.data = data;
		}
		
		public Collection<VisualMorphData> getData()
		{
			return data;
		}
	}
}
