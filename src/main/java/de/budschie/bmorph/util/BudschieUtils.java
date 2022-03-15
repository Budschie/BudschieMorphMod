package de.budschie.bmorph.util;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BudschieUtils
{
	public static boolean isLocalWorld()
	{
		return FMLEnvironment.dist == Dist.CLIENT && ServerSetup.server != null;
	}
	
	public static float getPhantomEaseFunction(float currentTime, float maxTime)
	{
		return (float) Math.pow((currentTime) / (maxTime), 1);
	}
	
	/** Convert a timestamp to a relative time that is left until this timestamp is reached. **/
	public static int convertToRelativeTime(int timestampUntilFinished)
	{
		return timestampUntilFinished - ServerSetup.server.getTickCount();
	}
	
	/** Convert a relative time to a timestamp. **/
	public static int convertToAbsoluteTime(int relativeTime)
	{
		return relativeTime + ServerSetup.server.getTickCount();
	}
}
