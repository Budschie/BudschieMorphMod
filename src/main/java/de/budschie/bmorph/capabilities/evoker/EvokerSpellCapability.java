package de.budschie.bmorph.capabilities.evoker;

public class EvokerSpellCapability implements IEvokerSpellCapability
{
	private int castingTicksLeft = 0;
	private int fangsTimePoint = 0;
	private double range = 0;
	
	@Override
	public int getCastingTicksLeft()
	{
		return castingTicksLeft;
	}

	@Override
	public void setCastingTicks(int amount)
	{
		this.castingTicksLeft = amount;
	}
	
	@Override
	public void setRange(double range)
	{
		this.range = range;
	}
	
	@Override
	public double getRange()
	{
		return range;
	}

	@Override
	public boolean isCasting()
	{
		return castingTicksLeft > 0;
	}

	@Override
	public void setFangsTimePoint(int fangsTimePoint)
	{
		this.fangsTimePoint = fangsTimePoint;
	}

	@Override
	public int getFangsTimePoint()
	{
		return fangsTimePoint;
	}
}
