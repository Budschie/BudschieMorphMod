package de.budschie.bmorph.capabilities.common;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class CommonCapabilityInstance<C> implements ICapabilitySerializable<CompoundTag> 
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
	
	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		deserializeAdditional(nbt, capability.resolve().get());
	}
	
	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag tag = new CompoundTag();
		
		serializeAdditional(tag, capability.resolve().get());
		
		return tag;
	}
	
	public abstract void deserializeAdditional(CompoundTag tag, C instance);
	public abstract void serializeAdditional(CompoundTag tag, C instance);
}
