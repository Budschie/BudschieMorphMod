package de.budschie.bmorph.json_integration;

import java.util.HashMap;

import de.budschie.bmorph.morph.FallbackMorphManager.SpecialDataHandler;
import net.minecraft.entity.EntityType;
import net.minecraftforge.eventbus.api.Event;

public class MorphNBTHandlersLoadedEvent extends Event
{
	private HashMap<EntityType<?>, SpecialDataHandler> handlerMap;
	
	public MorphNBTHandlersLoadedEvent(HashMap<EntityType<?>, SpecialDataHandler> handlerMap)
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
