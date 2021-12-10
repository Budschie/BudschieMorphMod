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
}
