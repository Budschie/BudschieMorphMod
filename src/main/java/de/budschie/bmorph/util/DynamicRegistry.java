package de.budschie.bmorph.util;

import java.util.Collection;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public abstract class DynamicRegistry<T extends IDynamicRegistryObject, SP>
{
	protected Logger logger = LogManager.getLogger();
	protected HashMap<ResourceLocation, T> entries = new HashMap<>();
	
	public void registerEntry(T entry)
	{
		if(entries.containsKey(entry.getResourceLocation()))
			throw new IllegalArgumentException(String.format("The key %s already exists.", entry.getResourceLocation()));
		else
		{
			logger.info("Registered " + entry.getResourceLocation());
			entries.put(entry.getResourceLocation(), entry);
			onRegister(entry);
		}
	}
	
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}
	
	public T getEntry(ResourceLocation key)
	{
		return entries.get(key);
	}
	
	public Collection<T> values()
	{
		return entries.values();
	}
	
	public boolean hasEntry(ResourceLocation key)
	{
		return entries.containsKey(key);
	}
	
	public void unregisterAll()
	{
		entries.forEach((name, entry) -> onUnregister(entry));
		entries.clear();
	}
	
	public void onRegister(T registeredObject) {}
	public void onUnregister(T unregisteredObject) {}
	
	public abstract SP getPacket();
	
	public void syncWithClients()
	{
		if(!entries.isEmpty())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), getPacket());
	}
	
	public void syncWithClient(ServerPlayer player)
	{
		if(!entries.isEmpty())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), getPacket());
	}	
}
