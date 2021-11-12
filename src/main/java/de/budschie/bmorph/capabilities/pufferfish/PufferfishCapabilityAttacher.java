package de.budschie.bmorph.capabilities.pufferfish;

import de.budschie.bmorph.main.References;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class PufferfishCapabilityAttacher implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "puffer_cap");
	
	public static final Capability<IPufferfishCapability> PUFFER_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	private LazyOptional<IPufferfishCapability> capability = LazyOptional.of(() -> new PufferfishCapability());
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return PUFFER_CAP.orEmpty(cap, capability);
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new PufferfishCapabilityAttacher());
	}

	@Override
	public CompoundTag serializeNBT()
	{
		IPufferfishCapability instance = capability.resolve().get();
		
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("originalPuffTime", instance.getOriginalPuffTime());
		nbt.putInt("puffTime", instance.getPuffTime());
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		IPufferfishCapability instance = capability.resolve().get();
		
		instance.setOriginalPuffTime(nbt.getInt("originalPuffTime"));
		instance.setPuffTime(nbt.getInt("puffTime"));
	}
}
