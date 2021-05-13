package de.budschie.bmorph.render_handler;

import java.util.ArrayList;

public class EntitySynchronizerRegistry
{
	private static ArrayList<IEntitySynchronizer> synchronizers = new ArrayList<>();
	
	public static synchronized void addEntitySynchronizer(IEntitySynchronizer synchronizer)
	{
		synchronizers.add(synchronizer);
	}
	
	public static ArrayList<IEntitySynchronizer> getSynchronizers()
	{
		return synchronizers;
	}
}
