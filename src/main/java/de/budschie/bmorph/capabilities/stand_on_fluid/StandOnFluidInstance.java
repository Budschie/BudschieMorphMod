package de.budschie.bmorph.capabilities.stand_on_fluid;

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
public class StandOnFluidInstance extends CommonCapabilityInstance<IStandOnFluidCapability>
{
	public static final Capability<IStandOnFluidCapability> STAND_ON_FLUID_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	public static final ResourceLocation STAND_ON_FLUID_CAP_NAME = new ResourceLocation(References.MODID, "stand_on_fluid");
	
	public StandOnFluidInstance()
	{
		super(STAND_ON_FLUID_CAP_NAME, STAND_ON_FLUID_CAP, StandOnFluidCapability::new);
	}
	
	@SubscribeEvent
	public static void onAttachingCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player)
		{
			event.addCapability(STAND_ON_FLUID_CAP_NAME, new StandOnFluidInstance());
		}
	}
}
