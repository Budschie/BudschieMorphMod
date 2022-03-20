package de.budschie.bmorph.capabilities.common;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class CommonCapabilityInstance<C> implements ICapabilityProvider
{
	protected ResourceLocation capabilityName;
	protected Capability<C> capabilityToken;
	
	protected LazyOptional<C> capability;
	
	public CommonCapabilityInstance(ResourceLocation capabilityName, Capability<C> capabilityToken, NonNullSupplier<C> capability)
	{
		this.capabilityName = capabilityName;
		this.capabilityToken = capabilityToken;
		this.capability = LazyOptional.of(capability);
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return capabilityToken.orEmpty(cap, capability);
	}
}
