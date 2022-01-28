package de.budschie.bmorph.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired directly after all the initially to the entity belonging
 * data transformers have been applied. This event can be used to add additional
 * tag entries to the output. <br>
 * <br>
 * It provides an alternative to adding a JSON to the morph_nbt directory. This
 * should <b>ONLY</b> be used when you want to have entity-sensitive morph data
 * modification or if you want to add morph data to a specific group of classes.
 * An example of this is the AgeableMob class. It is unfeasible to add a
 * morph_nbt entry to every existing animal in every mod, so this would be a
 * valid use case for this event. <br>
 * <br>
 * Note that it is not a valid use case to track specific morph data (such as
 * the color of the wool of a sheep), usage of this event should be kept at a
 * bare minimum to allow compatiblity for data packs.
 **/
public class MorphCreatedFromEntityEvent extends Event
{
	private CompoundTag tagIn;
	private CompoundTag tagOut;
	private Entity entity;
	
	public MorphCreatedFromEntityEvent(CompoundTag tagIn, CompoundTag tagOut, Entity entity)
	{
		this.tagIn = tagIn;
		this.tagOut = tagOut;
		this.entity = entity;
	}
	
	public void setTagOut(CompoundTag tagOut)
	{
		this.tagOut = tagOut;
	}
	
	public CompoundTag getTagIn()
	{
		return tagIn;
	}
	
	public CompoundTag getTagOut()
	{
		return tagOut;
	}
	
	public Entity getEntity()
	{
		return entity;
	}
}
