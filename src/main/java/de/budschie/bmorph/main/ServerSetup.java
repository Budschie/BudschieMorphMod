package de.budschie.bmorph.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.commands.BlacklistCommand;
import de.budschie.bmorph.commands.MorphCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class ServerSetup
{
	private static final Logger LOGGER = LogManager.getLogger();
//	public static MinecraftServer server;
		
	@SubscribeEvent
	public static void onServerStarting(final ServerStartingEvent event)
	{
		ConfigManager.INSTANCE.loadAll(event.getServer());
	}
	
	@SubscribeEvent
	public static void onServerReloaded(RegisterCommandsEvent event)
	{
		MorphCommand.registerCommands(event.getDispatcher());
		BlacklistCommand.registerCommand(event.getDispatcher());
		
		LOGGER.info("Registered morph commands.");
	}
	
	@SubscribeEvent
	public static void onServerStopping(final ServerStoppedEvent event)
	{
		ConfigManager.INSTANCE.serverShutdown(event.getServer());
		BMorphMod.DYNAMIC_ABILITY_REGISTRY.unregisterAll();
		BMorphMod.VISUAL_MORPH_DATA.clear();
		BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.unregisterAll();
	}
}
