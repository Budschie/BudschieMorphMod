package de.budschie.bmorph.capabilities.evoker;

public interface IEvokerSpellCapability
{
	/**
	 * {@return the amount of ticks that this player will be idle until the spell has been casted fully. }
	 */
	int getCastingTicksLeft();
	
	/**
	 * @param amount Set the ticks that this player will be idle until they can cast anything.
	 */
	void setCastingTicks(int amount);
	
	/**
	 * This won't be synced to the client.
	 * {@return the range of this spell.}
	 */
	double getRange();
	
	/**
	 * This won't be synced to the client.
	 * @param range The range this evoker attack shall have at most.
	 */
	void setRange(double range);
	
	/**
	 * This won't be synced to the client.
	 * @param fangsTimePoint The time point in ticks in which the fangs will be created.
	 */
	void setFangsTimePoint(int fangsTimePoint);
	
	/**
	 * {@return the time point in ticks in which the fangs will be created. This is not available on the client}
	 */
	int getFangsTimePoint();
	
	/**
	 * {@return {@code true} when this player is casting a spell, {@code false} otherwise.}
	 */
	boolean isCasting();
}
