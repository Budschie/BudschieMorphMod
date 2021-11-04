package de.budschie.bmorph.capabilities.pufferfish;

public interface IPufferfishCapability
{
	/** This method shall return a number between 0 and 2, indicating how puffed the pufferfish is. **/
	int getPuffState();
	
	/** This method retrieves the puff time. **/
	int getPuffTime();
	
	/** Returns the value that was originally passed to setPuffTime. **/
	int getOriginalPuffTime();
	
	/** Calls setPuffTime and setOriginalPuffTime to set puff values. **/
	void puff(int puffDuration);
	
	/** This method sets the puff time. **/
	void setPuffTime(int pufferfishTime);
	
	/** This method sets the puff time. **/
	void setOriginalPuffTime(int originalPuffTime);

}
