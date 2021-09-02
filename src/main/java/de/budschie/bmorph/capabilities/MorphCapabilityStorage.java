package de.budschie.bmorph.capabilities;

import de.budschie.bmorph.main.ServerSetup;
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
		
		if(instance.getCurrentMorphItem().isPresent())
			cap.put("currentMorphItem", instance.getCurrentMorphItem().get().serialize());
		
		if(instance.getCurrentMorphIndex().isPresent())
			cap.putInt("currentMorphIndex", instance.getCurrentMorphIndex().get());
		
		cap.put("morphList", instance.getMorphList().serializeNBT());
		
		cap.put("favouriteList", instance.getFavouriteList().serialize());
		
		cap.putInt("aggroDuration", Math.max(0, instance.getLastAggroDuration() - (ServerSetup.server.getTickCounter() - instance.getLastAggroTimestamp())));
		
		return cap;
	}

	@Override
	public void readNBT(Capability<IMorphCapability> capability, IMorphCapability instance, Direction side, INBT nbt)
	{
		CompoundNBT cap = (CompoundNBT) nbt;
		
		boolean hasItem = cap.contains("currentMorphItem");
		boolean hasIndex = cap.contains("currentMorphIndex");
		
		if(hasItem)
		{
			instance.setMorph(MorphHandler.deserializeMorphItem(cap.getCompound("currentMorphItem")));
		}
		
		if(hasIndex)
		{
			instance.setMorph(cap.getInt("currentMorphIndex"));
		}
		
		instance.getMorphList().deserializeNBT(cap.getCompound("morphList"));
		
		instance.getFavouriteList().deserialize(cap.getCompound("favouriteList"));
		
		instance.setLastAggroDuration(cap.getInt("aggroDuration"));
	}
}
