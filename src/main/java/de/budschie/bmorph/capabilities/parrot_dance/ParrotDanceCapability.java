package de.budschie.bmorph.capabilities.parrot_dance;

public class ParrotDanceCapability implements IParrotDanceCapability
{
	private boolean dancing = false;
	
	@Override
	public boolean isDancing()
	{
		return dancing;
	}

	@Override
	public void setDancing(boolean value)
	{
		this.dancing = value;
	}
}
