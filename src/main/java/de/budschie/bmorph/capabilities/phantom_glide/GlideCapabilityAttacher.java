package de.budschie.bmorph.capabilities.phantom_glide;

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
public class GlideCapabilityAttacher implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "glide_cap");
	
	public static final Capability<IGlideCapability> GLIDE_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	private LazyOptional<IGlideCapability> capability = LazyOptional.of(() -> new GlideCapability());
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return GLIDE_CAP.orEmpty(cap, capability);
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new GlideCapabilityAttacher());
	}

	@Override
	public CompoundTag serializeNBT()
	{
		IGlideCapability instance = capability.resolve().get();
		
		CompoundTag nbt = new CompoundTag();
		nbt.putString("glide", instance.getGlideStatus().name());
		nbt.putInt("chargeTime", instance.getChargeTime());
		nbt.putInt("maxChargeTime", instance.getMaxChargeTime());
		nbt.putInt("transitionTime", instance.getTransitionTime());
		
		if(instance.getGlideStatus() == GlideStatus.CHARGE && instance.getChargeDirection() != null)
		{
			nbt.putString("chargeDirection", instance.getChargeDirection().name());
		}
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		IGlideCapability instance = capability.resolve().get();
		
		instance.setGlideStatus(GlideStatus.valueOf(nbt.getString("glide")));
		instance.setChargeTime(nbt.getInt("chargeTime"));
		instance.setMaxChargeTime(nbt.getInt("maxChargeTime"));
		instance.setTransitionTime(nbt.getInt("transitionTime"));
		
		if(instance.getGlideStatus() == GlideStatus.CHARGE)
		{
			instance.setChargeDirection(ChargeDirection.valueOf(nbt.getString("chargeDirection")));
		}
	}
}
