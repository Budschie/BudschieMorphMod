package de.budschie.bmorph.capabilities.speed_of_morph_cap;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstance;
import de.budschie.bmorph.main.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class PlayerUsingSpeedOfMorphInstance extends CommonCapabilityInstance<IPlayerUsingSpeedOfMorph>
{
	public static final Capability<IPlayerUsingSpeedOfMorph> SPEED_OF_MORPH_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	public static final ResourceLocation SPEED_OF_MORPH_CAP_NAME = new ResourceLocation(References.MODID, "speed_of_morph");

	public PlayerUsingSpeedOfMorphInstance()
	{
		super(SPEED_OF_MORPH_CAP_NAME, SPEED_OF_MORPH_CAP, PlayerUsingSpeedOfMorph::new);
	}
	
	@SubscribeEvent
	public static void onAttachingCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player && player.level.isClientSide())
		{
			event.addCapability(SPEED_OF_MORPH_CAP_NAME, new PlayerUsingSpeedOfMorphInstance());
		}
	}
}
