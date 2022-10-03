package de.budschie.bmorph.capabilities.speed_of_morph_cap;

/**
 * The purpose of this class is to let the client know when to multiply the acceleration of the player by the speed of the morph the player is currently morphed into.
 */
public interface IPlayerUsingSpeedOfMorph
{
	boolean isUsingSpeedOfMorph();
	void setUsingSpeedOfMorph(boolean value);
}
