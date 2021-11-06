package de.budschie.bmorph.network;

import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.network.DisposePlayerMorphData.DisposePlayerMorphDataPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class DisposePlayerMorphData implements ISimpleImplPacket<DisposePlayerMorphDataPacket>
{

	@Override
	public void encode(DisposePlayerMorphDataPacket packet, PacketBuffer buffer)
	{
		buffer.writeUniqueId(packet.getToDispose());
	}

	@Override
	public DisposePlayerMorphDataPacket decode(PacketBuffer buffer)
	{
		return new DisposePlayerMorphDataPacket(buffer.readUniqueId());
	}

	@Override
	public void handle(DisposePlayerMorphDataPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnlyShit.disposePlayerMorphData(packet.getToDispose()));
		});
	}
	
	public static class DisposePlayerMorphDataPacket
	{
		private UUID toDispose;
		
		public DisposePlayerMorphDataPacket(UUID toDispose)
		{
			this.toDispose = toDispose;
		}
		
		public UUID getToDispose()
		{
			return toDispose;
		}
	}
}
