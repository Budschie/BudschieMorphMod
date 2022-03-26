package de.budschie.bmorph.capabilities.stand_on_fluid;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.material.Fluid;

public class StandOnFluidCapability implements IStandOnFluidCapability
{
	private ArrayList<Fluid> fluids = new ArrayList<>();
	
	@Override
	public List<Fluid> getAllowedFluids()
	{
		return fluids;
	}

	@Override
	public void addAllowedFluid(Fluid fluid)
	{
		this.fluids.add(fluid);
	}

	@Override
	public void removeAllowedFluid(Fluid fluid)
	{
		this.fluids.remove(fluid);
	}

	@Override
	public boolean containsFluid(Fluid fluid)
	{
		return fluids.contains(fluid);
	}
}
