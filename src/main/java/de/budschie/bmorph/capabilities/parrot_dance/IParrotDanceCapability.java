package de.budschie.bmorph.capabilities.parrot_dance;

public interface IParrotDanceCapability
{
	/** This variable indicates whether the parrot is dancing or not. **/
	boolean isDancing();

	/**
	 * This method is a setter for the value that is returned by
	 * {@link IParrotDanceCapability#isDancing()}.
	 **/
	void setDancing(boolean value);
}
