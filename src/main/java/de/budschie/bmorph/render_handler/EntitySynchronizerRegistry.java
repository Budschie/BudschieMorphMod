package de.budschie.bmorph.render_handler;

import java.util.ArrayList;

import net.minecraft.world.entity.Entity;

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
	
	public static ArrayList<IEntitySynchronizer> getSynchronizersForEntity(Entity entity)
	{
		ArrayList<IEntitySynchronizer> syncs = new ArrayList<>();
		
		// Create a list based on all the synchronizers that match the entity.
		for(IEntitySynchronizer sync : EntitySynchronizerRegistry.getSynchronizers())
		{
			if(sync.appliesToMorph(entity))
				syncs.add(sync);
		}
		
		return syncs;
	}
}
