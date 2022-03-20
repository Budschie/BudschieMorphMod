package de.budschie.bmorph.capabilities.sheep;

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
public class SheepCapabilityInstance extends CommonCapabilityInstanceSerializable<ISheepCapability>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "sheep_cap");
	
	public static final Capability<ISheepCapability> SHEEP_CAP = CapabilityManager.get(new CapabilityToken<>(){});

	public SheepCapabilityInstance()
	{
		super(CAPABILITY_NAME, SHEEP_CAP, SheepCapability::new);
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new SheepCapabilityInstance());
	}

	@Override
	public void deserializeAdditional(CompoundTag tag, ISheepCapability instance)
	{
		instance.setSheared(tag.getBoolean("IsSheared"));
	}

	@Override
	public void serializeAdditional(CompoundTag tag, ISheepCapability instance)
	{
		tag.putBoolean("IsSheared", instance.isSheared());
	}
}
