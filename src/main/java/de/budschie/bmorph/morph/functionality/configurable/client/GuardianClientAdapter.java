package de.budschie.bmorph.morph.functionality.configurable.client;

import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class GuardianClientAdapter
{
	private Predicate<Entity> isTracked;
	
	public GuardianClientAdapter(Predicate<Entity> isTracked)
	{
		this.isTracked = isTracked;
	}
	
	public boolean isTracked(Entity entity)
	{
		return isTracked.test(entity);
	}
	
	public abstract void enableAdapter();
	
	public abstract void disableAdapter();
	
	public abstract void playGuardianSound(PlayerEntity player);
}
