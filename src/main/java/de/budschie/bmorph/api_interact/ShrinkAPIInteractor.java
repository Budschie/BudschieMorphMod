package de.budschie.bmorph.api_interact;

import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.ModList;

public class ShrinkAPIInteractor
{
	private static IShrinkAPIInteract interactor;
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void init()
	{
		LOGGER.info("Initializing Shrink API support for bmorph...");
		
		boolean shrinkLoaded = ModList.get().isLoaded("shrink");
		
		LOGGER.info("Is shrink loaded: " + shrinkLoaded);
		
		if(shrinkLoaded)
		{
			LOGGER.info("Trying to load shrink API support...");
			
			try
			{
				interactor = (IShrinkAPIInteract) ShrinkAPIInteractor.class.getClassLoader().loadClass("de.budschie.bmorph.api_interact.ShrinkAPIInteract").getConstructor().newInstance();
				LOGGER.info("Successfully loaded support for the shrink API.");
			} 
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
			{
				LOGGER.info("An exception happened during the initialization of the shrink API support:");
				e.printStackTrace();
			}
		}
		else
		{
			LOGGER.info("Shrink not detected. Using dummy API instead.");
			interactor = new DummyShrinkAPIInteract();
		}
	}
	
	public static IShrinkAPIInteract getInteractor()
	{
		return interactor;
	}
}
