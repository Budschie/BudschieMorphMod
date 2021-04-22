package de.budschie.bmorph.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public interface ISimpleImplPacket<T>
{
	void encode(T packet, PacketBuffer buffer);
	T decode(PacketBuffer buffer);
	
	void handle(T packet, Supplier<NetworkEvent.Context> ctx);
}
