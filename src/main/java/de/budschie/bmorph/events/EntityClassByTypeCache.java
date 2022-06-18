package de.budschie.bmorph.events;

import java.util.HashMap;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.server.ServerLifecycleHooks;

public class EntityClassByTypeCache
{
	private static HashMap<EntityType<?>, Class<? extends Entity>> cache = new HashMap<>();
	
	/**
	 * Ugly fix for entity types not containing their class types. Hopefully, this
	 * doesn't create crashes lul.
	 **/
	@SuppressWarnings("unchecked")
	public static <T extends Entity> Class<T> getClassForEntityType(EntityType<T> entity)
	{
		Class<T> clazz = (Class<T>) cache.get(entity);
		
		if(clazz == null)
		{
			T entityInstance = entity.create(ServerLifecycleHooks.getCurrentServer().getAllLevels().iterator().next());
			clazz = (Class<T>) entityInstance.getClass();
			cache.put(entity, clazz);
		}
		
		return clazz;
	}
}
