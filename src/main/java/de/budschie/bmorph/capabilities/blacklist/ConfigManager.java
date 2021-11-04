package de.budschie.bmorph.capabilities.blacklist;

import java.util.HashMap;
import java.util.function.Supplier;

public enum ConfigManager
{	
	INSTANCE;
	
	private HashMap<Class<?>, Supplier<? extends WorldConfigHandler>> registeredConfigs = new HashMap<>();
	private HashMap<Class<?>, WorldConfigHandler> presentConfigs = new HashMap<>();
	
	public void saveAll()
	{
		presentConfigs.forEach((key, value) -> value.writeToFile());
	}
	
	public void serverShutdown()
	{
		saveAll();
		presentConfigs.clear();
	}
	
	public void loadAll()
	{
		registeredConfigs.forEach((key, value) ->
		{
			WorldConfigHandler instance = value.get();
			presentConfigs.put(key, instance);
			instance.readFromFile();
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz)
	{
		if(presentConfigs.containsKey(clazz))
			return (T) presentConfigs.get(clazz);
		
		throw new IllegalStateException("You may not retrieve world config handlers when they are not yet initialized.");
	}
	
	public <T extends WorldConfigHandler> void register(Class<T> clazz, Supplier<T> supplier)
	{
		registeredConfigs.put(clazz, supplier);
	}
}
