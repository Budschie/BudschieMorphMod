package de.budschie.bmorph.capabilities.phantom_glide;

public interface IGlideCapability
{
	/** Returns the current glide status. **/
	GlideStatus getGlideStatus();
	
	/** Sets the current glide status. **/
	void setGlideStatus(GlideStatus glideStatus);
	
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
	public default void glide()
	{
		setGlideStatus(GlideStatus.GLIDE);
		setChargeDirection(null);
	}
	
	/** Initiates standard status for the phantom. **/
	public default void standard()
	{
		setGlideStatus(GlideStatus.STANDARD);
		setChargeDirection(null);
	}
	
	/** Starts charging. **/
	public default void charge(int maxChargeTime, ChargeDirection direction)
	{
		setGlideStatus(GlideStatus.CHARGE);
		setChargeTime(maxChargeTime);
		setMaxChargeTime(maxChargeTime);
		setChargeDirection(direction);
	}
	
	public default void transitionIn(int transitionTime, int maxChargeTime, ChargeDirection direction)
	{
		setGlideStatus(GlideStatus.CHARGE_TRANSITION_IN);
		setChargeTime(maxChargeTime);
		setMaxChargeTime(maxChargeTime);
		setChargeDirection(direction);
		setTransitionTime(transitionTime);
		setMaxTransitionTime(transitionTime);
	}
	
	public default void transitionOut()
	{
		setGlideStatus(GlideStatus.CHARGE_TRANSITION_OUT);
	}
}
