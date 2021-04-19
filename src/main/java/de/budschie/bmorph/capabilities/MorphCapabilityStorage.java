package de.budschie.bmorph.capabilities;

import java.util.Optional;

import de.budschie.bmorph.morph.MorphHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class MorphCapabilityStorage implements IStorage<IMorphCapability>
{
	@Override
	public INBT writeNBT(Capability<IMorphCapability> capability, IMorphCapability instance, Direction side)
	{
		CompoundNBT cap = new CompoundNBT();
		
		if(instance.getCurrentMorph().isPresent())
			cap.put("currentMorph", instance.getCurrentMorph().get().serialize());
		
		return cap;
	}

	@Override
	public void readNBT(Capability<IMorphCapability> capability, IMorphCapability instance, Direction side, INBT nbt)
	{
		CompoundNBT cap = (CompoundNBT) nbt;
		
		boolean rs = cap.contains("currentMorph");
		
		if(rs)
		{
			instance.setCurrentMorph(Optional.of(MorphHandler.deserializeMorphItem(cap.getCompound("currentMorph"))));
		}		
	}
}
