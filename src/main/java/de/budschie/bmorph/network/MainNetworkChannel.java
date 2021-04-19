package de.budschie.bmorph.network;

import de.budschie.bmorph.main.References;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class MainNetworkChannel
{
	public static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(References.MODID, "main"), 
			() -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	
	private static int id = 0;
		
	public static void registerMainNetworkChannels()
	{
		INSTANCE.registerMessage(id++, MorphCapabilitySynchronizer.MorphPacket.class, MorphCapabilitySynchronizer::encode,
				MorphCapabilitySynchronizer::decode, MorphCapabilitySynchronizer::handle);
	}
}
