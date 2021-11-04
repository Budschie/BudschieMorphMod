package de.budschie.bmorph.capabilities.pufferfish;

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
public class PufferfishCapabilityAttacher implements ICapabilitySerializable<CompoundNBT>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "puffer_cap");
	
	@CapabilityInject(IPufferfishCapability.class)
	public static final Capability<IPufferfishCapability> PUFFER_CAP = null;
	
	private LazyOptional<IPufferfishCapability> capability = LazyOptional.of(PUFFER_CAP::getDefaultInstance);
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		if(cap == PUFFER_CAP)
		{
			return this.capability.cast();
		}
		
		return LazyOptional.empty();
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity)
			event.addCapability(CAPABILITY_NAME, new PufferfishCapabilityAttacher());
	}
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(IPufferfishCapability.class, new PufferfishCapabilityStorage(), () -> new PufferfishCapability());
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT) PUFFER_CAP.getStorage().writeNBT(PUFFER_CAP, capability.resolve().get(), null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		PUFFER_CAP.getStorage().readNBT(PUFFER_CAP, capability.resolve().get(), null, nbt);
	}
}
