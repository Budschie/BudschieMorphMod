package de.budschie.bmorph.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import de.budschie.bmorph.network.DataTransformerSynchronizer.DataTransfomerSynchronizerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class DataTransformerSynchronizer implements ISimpleImplPacket<DataTransfomerSynchronizerPacket>
{
	public static class DataTransfomerSynchronizerPacket
	{
		private Collection<DataTransformer> dataTransformers;
		
		public DataTransfomerSynchronizerPacket(Collection<DataTransformer> dataTransformers)
		{
			this.dataTransformers = dataTransformers;
		}
		
		public Collection<DataTransformer> getDataTransformers()
		{
			return dataTransformers;
		}
	}

	@Override
	public void encode(DataTransfomerSynchronizerPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.getDataTransformers().size());
		
		for(DataTransformer transformer : packet.getDataTransformers())
		{
			buffer.writeNbt(transformer.toNbt());
		}
	}

	@Override
	public DataTransfomerSynchronizerPacket decode(FriendlyByteBuf buffer)
	{
		ArrayList<DataTransformer> deserializedTransformers = new ArrayList<>();
		
		int dataTransformerAmount = buffer.readInt();
		
		for(int i = 0; i < dataTransformerAmount; i++)
		{
			deserializedTransformers.add(DataTransformer.valueOf(buffer.readNbt()));
		}
		
		return new DataTransfomerSynchronizerPacket(deserializedTransformers);
	}

	@Override
	public void handle(DataTransfomerSynchronizerPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.unregisterAll();
			
			for(DataTransformer transformer : packet.getDataTransformers())
			{
				BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.registerEntry(transformer);
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
}
