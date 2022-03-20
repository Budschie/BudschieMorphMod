package de.budschie.bmorph.capabilities.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.NonNullSupplier;

public abstract class CommonCapabilityInstanceSerializable<C> extends CommonCapabilityInstance<C> implements ICapabilitySerializable<CompoundTag> 
{
	public CommonCapabilityInstanceSerializable(ResourceLocation capabilityName, Capability<C> capabilityToken, NonNullSupplier<C> capability)
	{
		super(capabilityName, capabilityToken, capability);
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
