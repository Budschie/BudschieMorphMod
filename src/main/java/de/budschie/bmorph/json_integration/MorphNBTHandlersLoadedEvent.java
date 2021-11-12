package de.budschie.bmorph.json_integration;

import java.util.HashMap;

import de.budschie.bmorph.morph.fallback.IMorphNBTHandler;
import de.budschie.bmorph.morph.fallback.FallbackMorphManager.SpecialDataHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.Event;

public class MorphNBTHandlersLoadedEvent extends Event
{
	private HashMap<EntityType<?>, IMorphNBTHandler> handlerMap;
	
	public MorphNBTHandlersLoadedEvent(HashMap<EntityType<?>, IMorphNBTHandler> handlerMap)
	{
		this.handlerMap = handlerMap;
	}
	
	public boolean containsHandler(EntityType<?> entityType)
	{
		return handlerMap.containsKey(entityType);
	}
	
	public void addHandler(EntityType<?> entity, SpecialDataHandler handler)
	{
		handlerMap.put(entity, handler);
	}
}
