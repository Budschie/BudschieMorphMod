package de.budschie.bmorph.capabilities.pufferfish;

public class PufferfishCapability implements IPufferfishCapability
{	
	private int pufferfishTime = 0;
	private int originalPufftime = 0;

	@Override
	public int getPuffTime()
	{
		return pufferfishTime;
	}

	@Override
	public void setPuffTime(int pufferfishTime)
	{
		this.pufferfishTime = pufferfishTime;
	}

	@Override
	public int getPuffState()
	{
		if(pufferfishTime == 0)
			return 0;
		else if(((originalPufftime - 5) < pufferfishTime) || (pufferfishTime <= 5))
			return 1;
		else
			return 2;
	}

	@Override
	public int getOriginalPuffTime()
	{
		return originalPufftime;
	}

	@Override
	public void setOriginalPuffTime(int originalPuffTime)
	{
		this.originalPufftime = originalPuffTime;
	}

	@Override
	public void puff(int puffDuration)
	{
		setPuffTime(puffDuration);
		setOriginalPuffTime(puffDuration);
	}
}
