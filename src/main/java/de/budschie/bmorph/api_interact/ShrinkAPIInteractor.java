package de.budschie.bmorph.api_interact;

import java.lang.reflect.InvocationTargetException;

import net.minecraftforge.fml.ModList;

public class ShrinkAPIInteractor
{
	private static IShrinkAPIInteract interactor;
	
	public static void init()
	{
		System.out.println("Initializing Shrink API support...");
		
		boolean shrinkLoaded = ModList.get().isLoaded("shrink");
		
		System.out.println("Is shrink loaded: " + shrinkLoaded);
		
		if(shrinkLoaded)
		{
			System.out.println("Trying to load shrink API support...");
			
			try
			{
				interactor = (IShrinkAPIInteract) ShrinkAPIInteractor.class.getClassLoader().loadClass("de.budschie.bmorph.api_interact.ShrinkAPIInteract").getConstructor().newInstance();
				System.out.println("Successfully loaded support for the shrink API.");
			} 
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
			{
				System.out.println("An exception happened during the initialization of the shrink API support:");
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Shrink not detected. Using dummy API instead.");
			interactor = new DummyShrinkAPIInteract();
		}
	}
	
	public static IShrinkAPIInteract getInteractor()
	{
		return interactor;
	}
}
