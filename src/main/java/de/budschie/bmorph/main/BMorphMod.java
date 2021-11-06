package de.budschie.bmorph.main;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityAttacher;
import de.budschie.bmorph.entity.EntityRegistry;
import de.budschie.bmorph.gui.MorphGuiRegistry;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.morph.functionality.DynamicAbilityRegistry;
import de.budschie.bmorph.morph.player.PlayerMorphItem;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.render_handler.AbstractPlayerSynchronizer;
import de.budschie.bmorph.render_handler.EntitySynchronizerRegistry;
import de.budschie.bmorph.render_handler.LivingEntitySynchronzier;
import de.budschie.bmorph.render_handler.ParrotSynchronizer;
import de.budschie.bmorph.render_handler.PufferfishSynchronizer;
import de.budschie.bmorph.render_handler.SquidSynchronizer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntegerValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(value = References.MODID)
@EventBusSubscriber(bus = Bus.MOD)
public class BMorphMod
{
	public static RuleKey<BooleanValue> KEEP_MORPH_INVENTORY;
	public static RuleKey<BooleanValue> PREVENT_LOOKAT;
	public static RuleKey<IntegerValue> MORPH_AGGRO_DURATION;
	
	public static DynamicAbilityRegistry DYNAMIC_ABILITY_REGISTRY;
	
	public BMorphMod()
	{
		EntityRegistry.ENTITY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		EntityRegistry.SERIALIZER_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		AbilityRegistry.ABILITY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		MorphGuiRegistry.MORPH_GUI_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event)
	{
		ShrinkAPIInteractor.init();
		
		MorphCapabilityAttacher.register();
		PufferfishCapabilityAttacher.register();
		GuardianBeamCapabilityAttacher.register();
		
		ConfigManager.INSTANCE.register(BlacklistData.class, BlacklistData::new);
		
		System.out.println("Registered capabilities.");
		
		KEEP_MORPH_INVENTORY = GameRules.register("keepMorphInventory", Category.PLAYER, BooleanValue.create(true));
		PREVENT_LOOKAT = GameRules.register("preventLookat", Category.PLAYER, BooleanValue.create(false));
		MORPH_AGGRO_DURATION = GameRules.register("morphAggroDuration", Category.PLAYER, IntegerValue.create(200));
		
		MainNetworkChannel.registerMainNetworkChannels();
		
		MorphHandler.addMorphItem("player_morph_item", () -> new PlayerMorphItem());
		MorphHandler.addMorphItem("fallback_morph_item", () -> new FallbackMorphItem());
		
		MorphManagerHandlers.registerDefaultManagers();
		
		// VanillaFallbackMorphData.intialiseFallbackData();
		// APIInteractor.executeLoadClassIf(() -> ModList.get().isLoaded("betteranimalsplus"), "de.budschie.bmorph.morph.BetterAnimalsPlusFallbackMorphData");
		
		EntitySynchronizerRegistry.addEntitySynchronizer(new LivingEntitySynchronzier());
		EntitySynchronizerRegistry.addEntitySynchronizer(new ParrotSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new SquidSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new AbstractPlayerSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new PufferfishSynchronizer());
		
		DYNAMIC_ABILITY_REGISTRY = new DynamicAbilityRegistry();
	}	
}
