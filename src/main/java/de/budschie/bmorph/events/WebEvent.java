package de.budschie.bmorph.events;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Event;

public class WebEvent extends Event
{
	private final Vector3d oldWebSpeed;
	private Vector3d newWebSpeed;
	private boolean dirty = false;
	
	/** This event is being fired when a person walks through webs. **/
	public WebEvent(Vector3d oldWebSpeed)
	{
		this.oldWebSpeed = oldWebSpeed;
		this.newWebSpeed = new Vector3d(oldWebSpeed.x, oldWebSpeed.y, oldWebSpeed.z);
	}
	
	/**
	 * Returns the original speed multiplier that would be used if nothing would be
	 * modified.
	 **/
	public final Vector3d getOldWebSpeed()
	{
		return oldWebSpeed;
	}
	
	/**
	 * Returns the currently used web speed. Note that this is only a copy, use
	 * {@link WebEvent#setNewWebSpeed(Vector3d)} to set the new web speed.
	 **/
	public Vector3d getNewWebSpeed()
	{
		return new Vector3d(newWebSpeed.x, newWebSpeed.y, newWebSpeed.z);
	}
	
	/** Sets the new web speed and marks this web event as dirty. **/
	public void setNewWebSpeed(Vector3d newWebSpeed)
	{
		this.newWebSpeed = newWebSpeed;
		this.dirty = true;
	}
	
	/**
	 * Returns whether this web event is dirty or not. If it is dirty, we replace
	 * the vanilla logic with our logic, if, however, this is not dirty, we just
	 * pretend that this event didn't happen and ignore it.
	 **/
	public boolean isDirty()
	{
		return dirty;
	}
}
