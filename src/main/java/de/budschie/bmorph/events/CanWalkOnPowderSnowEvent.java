package de.budschie.bmorph.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class CanWalkOnPowderSnowEvent extends Event
{
	private Entity entity;
	
	public CanWalkOnPowderSnowEvent(Entity entity)
	{
		this.entity = entity;
	}
	
	public Entity getEntity()
	{
		return entity;
	}
	
	@Override
	public boolean hasResult()
	{
		return true;
	}
}
