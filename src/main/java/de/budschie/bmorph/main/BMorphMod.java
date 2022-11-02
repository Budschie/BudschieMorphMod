package de.budschie.bmorph.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.advancements.MorphedTrigger;
import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.entity.EntityRegistry;
import de.budschie.bmorph.gui.MorphGuiRegistry;
import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.VisualMorphDataRegistry;
import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.morph.functionality.DynamicAbilityRegistry;
import de.budschie.bmorph.morph.functionality.data_transformers.DataModifierRegistry;
import de.budschie.bmorph.morph.functionality.data_transformers.DynamicDataTransformerRegistry;
import de.budschie.bmorph.morph.player.PlayerMorphItem;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.predicates.PlayerAttributesPredicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
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
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static Key<BooleanValue> KEEP_MORPH_INVENTORY;
	public static Key<BooleanValue> PREVENT_LOOKAT;
	public static Key<BooleanValue> DO_MORPH_DROPS;
	public static Key<BooleanValue> ALLOW_MORPH_DELETION;
	public static Key<BooleanValue> ALLOW_MORPH_DROPPING;
	public static Key<BooleanValue> ALLOW_MORPH_TOOLS;
	public static Key<BooleanValue> INHERIT_MORPH_SPEED;
	public static Key<BooleanValue> SKIP_SPACE_RESTRICTION_CHECK;

	public static Key<IntegerValue> MORPH_AGGRO_DURATION;
	
	public static DynamicAbilityRegistry DYNAMIC_ABILITY_REGISTRY;
	public static DynamicDataTransformerRegistry DYNAMIC_DATA_TRANSFORMER_REGISTRY;
	public static VisualMorphDataRegistry VISUAL_MORPH_DATA;
	public static AbilityGroupRegistry ABILITY_GROUPS;
	
	public static final MorphedTrigger ACQUIRED_MORPH = new MorphedTrigger(new ResourceLocation(References.MODID, "acquired_morph"));
	public static final MorphedTrigger MORPHED_INTO = new MorphedTrigger(new ResourceLocation(References.MODID, "morphed_into"));
	public static final MorphedTrigger DEMORPHED_FROM = new MorphedTrigger(new ResourceLocation(References.MODID, "demorphed_from"));
	
	public BMorphMod()
	{
		EntityRegistry.ENTITY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		EntityRegistry.SERIALIZER_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		AbilityRegistry.ABILITY_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		DataModifierRegistry.DATA_MODIFIER_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		MorphGuiRegistry.MORPH_GUI_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
		MorphReasonRegistry.MORPH_REASON_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event)
	{
		ShrinkAPIInteractor.init();
		
		ConfigManager.INSTANCE.register(BlacklistData.class, BlacklistData::new);
		
		LOGGER.info("Registered capabilities.");
		
		KEEP_MORPH_INVENTORY = GameRules.register("keepMorphInventory", Category.PLAYER, BooleanValue.create(true));
		PREVENT_LOOKAT = GameRules.register("preventLookat", Category.PLAYER, BooleanValue.create(false));
		DO_MORPH_DROPS = GameRules.register("doMorphDrops", Category.DROPS, BooleanValue.create(true));
		MORPH_AGGRO_DURATION = GameRules.register("morphAggroDuration", Category.PLAYER, IntegerValue.create(200));
		ALLOW_MORPH_DELETION = GameRules.register("allowMorphDeletion", Category.PLAYER, BooleanValue.create(true));
		ALLOW_MORPH_DROPPING = GameRules.register("allowMorphDropping", Category.PLAYER, BooleanValue.create(true));
		ALLOW_MORPH_TOOLS = GameRules.register("allowMorphTools", Category.PLAYER, BooleanValue.create(true));
		INHERIT_MORPH_SPEED = GameRules.register("inheritMorphSpeed", Category.PLAYER, BooleanValue.create(false));
		SKIP_SPACE_RESTRICTION_CHECK = GameRules.register("skipSpaceRestrictionCheck", Category.PLAYER, BooleanValue.create(false));

		MainNetworkChannel.registerMainNetworkChannels();
		
		MorphHandler.addMorphItem("player_morph_item", () -> new PlayerMorphItem());
		MorphHandler.addMorphItem("fallback_morph_item", () -> new FallbackMorphItem());
		
		MorphManagerHandlers.registerDefaultManagers();
		
		// VanillaFallbackMorphData.intialiseFallbackData();
		// APIInteractor.executeLoadClassIf(() -> ModList.get().isLoaded("betteranimalsplus"), "de.budschie.bmorph.morph.BetterAnimalsPlusFallbackMorphData");
				
		DYNAMIC_ABILITY_REGISTRY = new DynamicAbilityRegistry();
		DYNAMIC_DATA_TRANSFORMER_REGISTRY = new DynamicDataTransformerRegistry();
		VISUAL_MORPH_DATA = new VisualMorphDataRegistry();
		ABILITY_GROUPS = new AbilityGroupRegistry();
		
		event.enqueueWork(() ->
		{
			CriteriaTriggers.register(ACQUIRED_MORPH);
			CriteriaTriggers.register(MORPHED_INTO);
			CriteriaTriggers.register(DEMORPHED_FROM);
			
			LootItemConditions.register(References.MODID + ":player_attributes", new PlayerAttributesPredicate.Serializer());
		});
	}	
}
