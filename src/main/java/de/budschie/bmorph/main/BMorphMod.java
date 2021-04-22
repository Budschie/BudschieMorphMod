package de.budschie.bmorph.main;

import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.commands.MorphCommand;
import de.budschie.bmorph.entity.EntityRegistry;
import de.budschie.bmorph.morph.FallbackMorphItem;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.PlayerMorphItem;
import de.budschie.bmorph.morph.VanillaFallbackMorphData;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value = References.MODID)
@EventBusSubscriber(bus = Bus.MOD)
public class BMorphMod
{
	public BMorphMod()
	{
		EntityRegistry.ENTITY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		EntityRegistry.SERIALIZER_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event)
	{
		MorphCapabilityAttacher.register();
		System.out.println("Registered capabilities.");
		
		MainNetworkChannel.registerMainNetworkChannels();
		
		MorphHandler.addMorphItem("player_morph_item", () -> new PlayerMorphItem());
		MorphHandler.addMorphItem("fallback_morph_item", () -> new FallbackMorphItem());
		
		MorphManagerHandlers.registerDefaultManagers();
		
		VanillaFallbackMorphData.intialiseFallbackData();
	}	
}
