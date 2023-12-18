package de.budschie.bmorph.util;

public class TickTimestamp
{
	private int timestamp;
	
	public TickTimestamp(int ticksElapsed)
	{
		this.timestamp = BudschieUtils.getUniversalTickTime() - ticksElapsed;
	}
	
	public TickTimestamp()
	{
		this.timestamp = BudschieUtils.getUniversalTickTime();
	}
	
	public int getTimeElapsed()
	{
		return BudschieUtils.getUniversalTickTime() - timestamp;
	}
	
	public int getTimestamp()
	{
		return timestamp;
	}
}
