package de.budschie.bmorph.capabilities.phantom_glide;

import net.minecraft.world.entity.player.Player;

public interface IGlideCapability
{
	/** Returns the current glide status. **/
	GlideStatus getGlideStatus();
	
	/** Sets the current glide status. This shall fire an event when the glide status was changed (when the old glide status != the new glide status). Player may be null, but this prevents firing an event, and player should really not be null. **/
	void setGlideStatus(GlideStatus glideStatus, Player player);
	
	/** Returns the current status time. **/
	int getChargeTime();
	
	/** Sets the current status time. **/
	void setChargeTime(int time);
	
	/** Retrieves the max charge time. Used to interpolate values on the client. **/
	int getMaxChargeTime();
	
	/** Sets the max charge time. Used to interpolate values on the client. **/
	void setMaxChargeTime(int time);
	
	/** Retrieves the current transition time. (The time in ticks that the phantom needs to transition from gliding to charging). **/
	int getTransitionTime();
	
	/** Sets the current transition time. **/
	void setTransitionTime(int time);
	
	/** Retrieves the max transition time. **/
	int getMaxTransitionTime();
	
	/** Sets the max transition time. **/
	void setMaxTransitionTime(int time);
	
	/** Retrieves the charge direction. **/
	ChargeDirection getChargeDirection();
	
	/** Sets the charge direction. **/
	void setChargeDirection(ChargeDirection direction);
	
	/** Initiates gliding of the phantom. **/
	public default void glide(Player player)
	{
		setGlideStatus(GlideStatus.GLIDE, player);
		setChargeDirection(null);
	}
	
	/** Initiates standard status for the phantom. **/
	public default void standard(Player player)
	{
		setGlideStatus(GlideStatus.STANDARD, player);
		setChargeDirection(null);
	}
	
	/** Starts charging. **/
	public default void charge(int maxChargeTime, ChargeDirection direction, Player player)
	{
		setGlideStatus(GlideStatus.CHARGE, player);
		setChargeTime(maxChargeTime);
		setMaxChargeTime(maxChargeTime);
		setChargeDirection(direction);
	}
	
	public default void transitionIn(int transitionTime, int maxChargeTime, ChargeDirection direction, Player player)
	{
		setGlideStatus(GlideStatus.CHARGE_TRANSITION_IN, player);
		setChargeTime(maxChargeTime);
		setMaxChargeTime(maxChargeTime);
		setChargeDirection(direction);
		setTransitionTime(transitionTime);
		setMaxTransitionTime(transitionTime);
	}
	
	public default void transitionOut(Player player)
	{
		setGlideStatus(GlideStatus.CHARGE_TRANSITION_OUT, player);
	}
}
