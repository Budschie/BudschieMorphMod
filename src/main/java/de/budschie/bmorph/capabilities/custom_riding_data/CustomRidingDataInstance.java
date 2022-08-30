package de.budschie.bmorph.capabilities.custom_riding_data;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstance;
import de.budschie.bmorph.main.References;
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
public class CustomRidingDataInstance extends CommonCapabilityInstance<ICustomRidingData>
{
	public static final Capability<ICustomRidingData> CUSTOM_RIDING_DATA_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	public static final ResourceLocation CUSTOM_RIDING_DATA_CAP_NAME = new ResourceLocation(References.MODID, "custom_riding_data");
	
	public CustomRidingDataInstance()
	{
		super(CUSTOM_RIDING_DATA_CAP_NAME, CUSTOM_RIDING_DATA_CAP, CustomRidingData::new);
	}
	
	@SubscribeEvent
	public static void onAttachingCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player)
		{
			event.addCapability(CUSTOM_RIDING_DATA_CAP_NAME, new CustomRidingDataInstance());
		}
	}
}
