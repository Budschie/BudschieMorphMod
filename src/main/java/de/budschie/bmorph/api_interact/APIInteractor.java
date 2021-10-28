package de.budschie.bmorph.api_interact;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class APIInteractor
{
	/** This class dynamically loads the given {@code className} and casts it to a Runnable which gets
	 * immediately called if the boolean that is being supplied to this method is {@code true}.
	 * This method is used for loading compatability patches.  **/
	public static void executeLoadClassIf(Supplier<Boolean> supplier, String className)
	{
		// If the class should be loaded
		if(supplier.get())
		{
			try
			{
				Runnable runnable = (Runnable) APIInteractor.class.getClassLoader().loadClass(className).getConstructor().newInstance();
				runnable.run();
			} 
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
}
