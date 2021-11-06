package de.budschie.bmorph.capabilities.guardian;

import de.budschie.bmorph.main.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class GuardianBeamCapabilityAttacher implements ICapabilitySerializable<CompoundNBT>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "guardian_beam_cap");
	
	@CapabilityInject(IGuardianBeamCapability.class)
	public static final Capability<IGuardianBeamCapability> GUARDIAN_BEAM_CAP = null;
	
	private LazyOptional<IGuardianBeamCapability> capability = LazyOptional.of(GUARDIAN_BEAM_CAP::getDefaultInstance);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == GUARDIAN_BEAM_CAP)
		{
			return this.capability.cast();
		}
		
		return LazyOptional.empty();
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity)
			event.addCapability(CAPABILITY_NAME, new GuardianBeamCapabilityAttacher());
	}
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(IGuardianBeamCapability.class, new GuardianBeamCapabilityStorage(), () -> new GuardianBeamCapability());
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT) GUARDIAN_BEAM_CAP.getStorage().writeNBT(GUARDIAN_BEAM_CAP, capability.resolve().get(), null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		GUARDIAN_BEAM_CAP.getStorage().readNBT(GUARDIAN_BEAM_CAP, capability.resolve().get(), null, nbt);
	}
}
