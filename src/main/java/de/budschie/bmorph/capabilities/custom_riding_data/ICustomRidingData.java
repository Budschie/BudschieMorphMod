package de.budschie.bmorph.capabilities.custom_riding_data;

import java.util.Optional;

public interface ICustomRidingData
{
	public Optional<Double> getCustomRidingOffset();
	public void setCustomRidingOffset(Optional<Double> customRidingOffset);
}
