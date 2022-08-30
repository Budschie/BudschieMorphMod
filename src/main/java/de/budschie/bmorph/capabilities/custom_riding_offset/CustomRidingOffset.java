package de.budschie.bmorph.capabilities.custom_riding_offset;

import java.util.Optional;

public class CustomRidingOffset implements ICustomRidingOffset
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
