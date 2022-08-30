package de.budschie.bmorph.capabilities.custom_riding_data;

import java.util.Optional;

public class CustomRidingData implements ICustomRidingData
{
	private Optional<Double> customRidingOffset = Optional.empty();

	@Override
	public Optional<Double> getCustomRidingOffset()
	{
		return customRidingOffset;
	}

	@Override
	public void setCustomRidingOffset(Optional<Double> customRidingOffset)
	{
		this.customRidingOffset = customRidingOffset;
	}
}
