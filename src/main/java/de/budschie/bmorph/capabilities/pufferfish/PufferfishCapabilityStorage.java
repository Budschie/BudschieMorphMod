package de.budschie.bmorph.capabilities.pufferfish;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class PufferfishCapabilityStorage implements IStorage<IPufferfishCapability>
{
	@Override
	public INBT writeNBT(Capability<IPufferfishCapability> capability, IPufferfishCapability instance, Direction side)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("originalPuffTime", instance.getOriginalPuffTime());
		nbt.putInt("puffTime", instance.getPuffTime());
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<IPufferfishCapability> capability, IPufferfishCapability instance, Direction side, INBT nbt)
	{
		CompoundNBT casted = (CompoundNBT) nbt;
		
		instance.setOriginalPuffTime(casted.getInt("originalPuffTime"));
		instance.setPuffTime(casted.getInt("puffTime"));
	}
}
