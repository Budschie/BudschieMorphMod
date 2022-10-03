package de.budschie.bmorph.capabilities.speed_of_morph_cap;

public class PlayerUsingSpeedOfMorph implements IPlayerUsingSpeedOfMorph
{
	private boolean speedOfMorph = false; 

	@Override
	public boolean isUsingSpeedOfMorph()
	{
		return speedOfMorph;
	}

	@Override
	public void setUsingSpeedOfMorph(boolean value)
	{
		this.speedOfMorph = value;
	}
}
