package de.budschie.bmorph.capabilities.phantom_glide;

import net.minecraft.world.phys.Vec3;

public enum ChargeDirection
{
	UP(new Vec3(0, 1, 0), 90), DOWN(new Vec3(0, -1, 0), -90);
	
	private Vec3 movementDirection;
	private float rotX;
	
	private ChargeDirection(Vec3 movementDirection, float rotX)
	{
		this.movementDirection = movementDirection;
		this.rotX = rotX;
	}
	
	public Vec3 getMovementDirection()
	{
		return movementDirection;
	}
	
	public float getRotX()
	{
		return rotX;
	}
}
