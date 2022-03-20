package de.budschie.bmorph.capabilities.parrot_dance;

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

@EventBusSubscriber
public class ParrotDanceCapabilityInstance extends CommonCapabilityInstanceSerializable<IParrotDanceCapability>
{
	public static final Capability<IParrotDanceCapability> PARROT_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	
	public static final ResourceLocation PARROT_CAP_NAME = new ResourceLocation(References.MODID, "parrot_dance_cap");
	
	public ParrotDanceCapabilityInstance()
	{
		super(PARROT_CAP_NAME, PARROT_CAP, ParrotDanceCapability::new);
	}
	
	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(PARROT_CAP_NAME, new ParrotDanceCapabilityInstance());
	}

	@Override
	public void deserializeAdditional(CompoundTag tag, IParrotDanceCapability instance)
	{
		instance.setDancing(tag.getBoolean("dancing"));
	}

	@Override
	public void serializeAdditional(CompoundTag tag, IParrotDanceCapability instance)
	{
		tag.putBoolean("dancing", instance.isDancing());
	}	
}
