package de.budschie.bmorph.capabilities;

import java.util.concurrent.Callable;

public class MorphCapabilityFactory implements Callable<IMorphCapability>
{
	@Override
	public IMorphCapability call() throws Exception
	{
		return new DefaultMorphCapability();
	}
}
