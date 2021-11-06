package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class GuardianBeamCapabilityStorage implements IStorage<IGuardianBeamCapability>
{
	@Override
	public INBT writeNBT(Capability<IGuardianBeamCapability> capability, IGuardianBeamCapability instance, Direction side)
	{
		CompoundNBT nbt = new CompoundNBT();
		instance.getAttackedEntityServer().ifPresent(uuid -> nbt.putUniqueId("attacked_entity", uuid));
		nbt.putInt("attack_progression", instance.getAttackProgression());
		
		return nbt;
	}

	@Override
	public void readNBT(Capability<IGuardianBeamCapability> capability, IGuardianBeamCapability instance, Direction side, INBT nbt)
	{
		CompoundNBT casted = (CompoundNBT) nbt;
				
		if(casted.contains("attacked_entity"))
			instance.setAttackedEntityServer(Optional.of(casted.getUniqueId("attacked_entity")));
		
		instance.setAttackProgression(casted.getInt("attack_progression"));
	}
}
