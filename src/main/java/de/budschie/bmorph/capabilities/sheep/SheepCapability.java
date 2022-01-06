package de.budschie.bmorph.capabilities.sheep;

public class SheepCapability implements ISheepCapability
{
	private boolean sheared = false;
	
	@Override
	public boolean isSheared()
	{
		return sheared;
	}

	@Override
	public void setSheared(boolean value)
	{
		this.sheared = value;
	}
}
