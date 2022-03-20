package de.budschie.bmorph.capabilities.stand_on_fluid;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstanceSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.NonNullSupplier;

public class StandOnFluidInstance extends CommonCapabilityInstanceSerializable<IStandOnFluidCapability>
{

	public StandOnFluidInstance(ResourceLocation capabilityName, Capability<IStandOnFluidCapability> capabilityToken,
			NonNullSupplier<IStandOnFluidCapability> capability)
	{
		super(capabilityName, capabilityToken, capability);
	}

	@Override
	public void deserializeAdditional(CompoundTag tag, IStandOnFluidCapability instance)
	{
		
	}

	@Override
	public void serializeAdditional(CompoundTag tag, IStandOnFluidCapability instance)
	{
		
	}
}
