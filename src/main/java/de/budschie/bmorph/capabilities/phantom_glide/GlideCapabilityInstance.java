package de.budschie.bmorph.capabilities.phantom_glide;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstanceSerializable;
import de.budschie.bmorph.main.References;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class GlideCapabilityInstance extends CommonCapabilityInstanceSerializable<IGlideCapability>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "glide_cap");
	
	public static final Capability<IGlideCapability> GLIDE_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	public GlideCapabilityInstance()
	{
		super(CAPABILITY_NAME, GLIDE_CAP, GlideCapability::new);
	}
		
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new GlideCapabilityInstance());
	}

	@Override
	public void deserializeAdditional(CompoundTag tag, IGlideCapability instance)
	{
		instance.setGlideStatus(GlideStatus.valueOf(tag.getString("glide")), null);
		instance.setChargeTime(tag.getInt("chargeTime"));
		instance.setMaxChargeTime(tag.getInt("maxChargeTime"));
		instance.setTransitionTime(tag.getInt("transitionTime"));
		
		if(instance.getGlideStatus() == GlideStatus.CHARGE)
		{
			instance.setChargeDirection(ChargeDirection.valueOf(tag.getString("chargeDirection")));
		}
	}

	@Override
	public void serializeAdditional(CompoundTag tag, IGlideCapability instance)
	{
		tag.putString("glide", instance.getGlideStatus().name());
		tag.putInt("chargeTime", instance.getChargeTime());
		tag.putInt("maxChargeTime", instance.getMaxChargeTime());
		tag.putInt("transitionTime", instance.getTransitionTime());
		
		if(instance.getGlideStatus() == GlideStatus.CHARGE && instance.getChargeDirection() != null)
		{
			tag.putString("chargeDirection", instance.getChargeDirection().name());
		}
	}
}
