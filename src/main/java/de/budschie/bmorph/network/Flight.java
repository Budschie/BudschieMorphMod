package de.budschie.bmorph.network;

import java.util.function.Supplier;

import de.budschie.bmorph.network.Flight.FlightPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class Flight implements ISimpleImplPacket<FlightPacket>
{
	public static class FlightPacket
	{
		private boolean shouldFly;
		
		public FlightPacket(boolean shouldFly)
		{
			this.shouldFly = shouldFly;
		}
		
		public boolean shouldFly()
		{
			return shouldFly;
		}
	}

	@Override
	public void encode(FlightPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.shouldFly());
	}

	@Override
	public FlightPacket decode(FriendlyByteBuf buffer)
	{
		return new FlightPacket(buffer.readBoolean());
	}

	@Override
	public void handle(FlightPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			Minecraft.getInstance().player.getAbilities().flying = packet.shouldFly();
			
			ctx.get().setPacketHandled(true);
		});
	}
}
