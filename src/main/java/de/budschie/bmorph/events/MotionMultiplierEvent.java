package de.budschie.bmorph.events;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Event;

public class MotionMultiplierEvent extends Event
{
	private final Vector3d oldMotionMultiplier;
	private Vector3d newMotionMultiplier;
	private final Entity entity;
	private final BlockState blockState;
	private final World world;
	private boolean dirty = false;
	
	/** This event is being fired when {@link Entity#setMotionMultiplier(net.minecraft.block.BlockState, Vector3d)} is called. **/
	public MotionMultiplierEvent(Vector3d oldMotionMultiplier, Entity entity, BlockState blockState, World world)
	{
		this.oldMotionMultiplier = oldMotionMultiplier;
		this.newMotionMultiplier = oldMotionMultiplier;
		this.entity = entity;
		this.blockState = blockState;
		this.world = world;
	}
	
	/** Returns the entity whose motion multiplier setter was called. **/
	public Entity getEntity()
	{
		return entity;
	}
	
	/**
	 * Gets the block state of the block that caused the call of
	 * {@link Entity#setMotionMultiplier(BlockState, Vector3d)} at the time of the
	 * collision. Note that the actual block might have changed since then.
	 **/
	public BlockState getBlockState()
	{
		return blockState;
	}
	
	/** Returns the world in which the collision happened. **/
	public World getWorld()
	{
		return world;
	}
		
	/**
	 * Returns the original motion multiplier that would be used if nothing would be
	 * modified.
	 **/
	public final Vector3d getOriginalMotionMultiplier()
	{
		return oldMotionMultiplier;
	}
	
	/**
	 * Returns the currently used motion multiplier. Note that this is only a copy, use
	 * {@link MotionMultiplierEvent#setNewMotionMultiplier(Vector3d)} to set the new web speed.
	 **/
	public Vector3d getNewMotionMultiplier()
	{
		return new Vector3d(newMotionMultiplier.x, newMotionMultiplier.y, newMotionMultiplier.z);
	}
	
	/** Sets the new motion multiplier and marks this motion multiplier event as dirty. **/
	public void setNewMotionMultiplier(Vector3d newWebSpeed)
	{
		this.newMotionMultiplier = newWebSpeed;
		this.dirty = true;
	}
	
	/**
	 * Returns whether this motion multiplier event is dirty or not. If it is dirty, we replace
	 * the vanilla logic with our logic, if, however, this is not dirty, we just
	 * pretend that this event didn't happen and ignore it.
	 **/
	public boolean isDirty()
	{
		return dirty;
	}
	
	@Override
	public boolean isCancelable()
	{
		return true;
	}
}
