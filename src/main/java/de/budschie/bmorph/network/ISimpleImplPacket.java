package de.budschie.bmorph.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface ISimpleImplPacket<T>
{
	void encode(T packet, FriendlyByteBuf buffer);
	T decode(FriendlyByteBuf buffer);
	
	void handle(T packet, Supplier<NetworkEvent.Context> ctx);
}
