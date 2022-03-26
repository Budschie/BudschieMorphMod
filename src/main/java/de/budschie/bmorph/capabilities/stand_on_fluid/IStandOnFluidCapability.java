package de.budschie.bmorph.capabilities.stand_on_fluid;

import java.util.List;

import net.minecraft.world.level.material.Fluid;

public interface IStandOnFluidCapability
{
	List<Fluid> getAllowedFluids();
	boolean containsFluid(Fluid fluid);
	void addAllowedFluid(Fluid fluid);
	void removeAllowedFluid(Fluid fluid);
}
