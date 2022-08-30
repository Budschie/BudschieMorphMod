package de.budschie.bmorph.main;

import org.lwjgl.glfw.GLFW;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import de.budschie.bmorph.entity.EntityRegistry;
import de.budschie.bmorph.entity.rendering.MorphEntityRenderer;
import de.budschie.bmorph.morph.player.AdvancedAbstractClientPlayerEntity;
import de.budschie.bmorph.morph.player.UglyHackThatDoesntWork;
import de.budschie.bmorph.render_handler.AbstractPlayerSynchronizer;
import de.budschie.bmorph.render_handler.CommonEntitySynchronizer;
import de.budschie.bmorph.render_handler.EnderDragonSynchronizer;
import de.budschie.bmorph.render_handler.EndermanSynchronizer;
import de.budschie.bmorph.render_handler.EntitySynchronizerRegistry;
import de.budschie.bmorph.render_handler.EvokerSynchronizer;
import de.budschie.bmorph.render_handler.GuardianEntitySynchronizer;
import de.budschie.bmorph.render_handler.LivingEntitySynchronzier;
import de.budschie.bmorph.render_handler.MobSynchronizer;
import de.budschie.bmorph.render_handler.ParrotSynchronizer;
import de.budschie.bmorph.render_handler.PhantomSynchronizer;
import de.budschie.bmorph.render_handler.PufferfishSynchronizer;
import de.budschie.bmorph.render_handler.SheepSynchronizer;
import de.budschie.bmorph.render_handler.SquidSynchronizer;
import de.budschie.bmorph.render_handler.TamableSynchronizer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class ClientSetup
{
	// Key to press when you want to activate an active ability.
	public static final KeyMapping USE_ABILITY_KEY = new KeyMapping("key.bmorph.use_ability", GLFW.GLFW_KEY_Z, "key.bmorph.bmorph_category");
	// Toggles the morph UI on or off
	public static final KeyMapping TOGGLE_MORPH_UI = new KeyMapping("key.bmorph.toggle_morph", GLFW.GLFW_KEY_SEMICOLON, "key.bmorph.bmorph_category");
	// Changes the favourite status of a selected morph
	public static final KeyMapping TOGGLE_MORPH_FAVOURITE = new KeyMapping("key.bmorph.toggle_morph_favourite", GLFW.GLFW_KEY_APOSTROPHE, "key.bmorph.bmorph_category");
	// Scrolls up, down, left or right
	public static final KeyMapping SCROLL_UP_MORPH_UI = new KeyMapping("key.bmorph.scroll_up", GLFW.GLFW_KEY_UP, "key.bmorph.bmorph_category");
	public static final KeyMapping SCROLL_DOWN_MORPH_UI = new KeyMapping("key.bmorph.scroll_down", GLFW.GLFW_KEY_DOWN, "key.bmorph.bmorph_category");
	public static final KeyMapping SCROLL_LEFT_MORPH_UI = new KeyMapping("key.bmorph.scroll_left", GLFW.GLFW_KEY_LEFT, "key.bmorph.bmorph_category");
	public static final KeyMapping SCROLL_RIGHT_MORPH_UI = new KeyMapping("key.bmorph.scroll_right", GLFW.GLFW_KEY_RIGHT, "key.bmorph.bmorph_category");
	// Changes to the next morph ui
	public static final KeyMapping NEXT_MORPH_UI = new KeyMapping("key.bmorph.next", GLFW.GLFW_KEY_KP_ADD, "key.bmorph.bmorph_category");
	// Changes to the previous morph UI
	public static final KeyMapping PREVIOUS_MORPH_UI = new KeyMapping("key.bmorph.previous", GLFW.GLFW_KEY_KP_SUBTRACT, "key.bmorph.bmorph_category");
	// Morph to the currently selected morph
	public static final KeyMapping MORPH_UI = new KeyMapping("key.bmorph.morph", GLFW.GLFW_KEY_ENTER, "key.bmorph.bmorph_category");
	
	// Completely deletes current morph
	public static final KeyMapping DELETE_CURRENT_MORPH = new KeyMapping("key.bmorph.delete_current", GLFW.GLFW_KEY_BACKSPACE, "key.bmorph.bmorph_category");
	// Drops the currently selected morph
	public static final KeyMapping DROP_CURRENT_MORPH = new KeyMapping("key.bmorph.drop_current", GLFW.GLFW_KEY_R, "key.bmorph.bmorph_category");
	
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event)
	{		
		EntityRenderers.register(EntityRegistry.MORPH_ENTITY.get(), manager -> new MorphEntityRenderer(manager));
		ClientRegistry.registerKeyBinding(USE_ABILITY_KEY);
		ClientRegistry.registerKeyBinding(SCROLL_DOWN_MORPH_UI);
		ClientRegistry.registerKeyBinding(SCROLL_UP_MORPH_UI);
		ClientRegistry.registerKeyBinding(SCROLL_LEFT_MORPH_UI);
		ClientRegistry.registerKeyBinding(SCROLL_RIGHT_MORPH_UI);
		ClientRegistry.registerKeyBinding(TOGGLE_MORPH_UI);
		ClientRegistry.registerKeyBinding(TOGGLE_MORPH_FAVOURITE);
		ClientRegistry.registerKeyBinding(NEXT_MORPH_UI);
		ClientRegistry.registerKeyBinding(PREVIOUS_MORPH_UI);
		ClientRegistry.registerKeyBinding(MORPH_UI);
		
		ClientRegistry.registerKeyBinding(DELETE_CURRENT_MORPH);
		ClientRegistry.registerKeyBinding(DROP_CURRENT_MORPH);
		
		EntitySynchronizerRegistry.addEntitySynchronizer(new CommonEntitySynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new LivingEntitySynchronzier());
		EntitySynchronizerRegistry.addEntitySynchronizer(new SquidSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new AbstractPlayerSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new PufferfishSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new GuardianEntitySynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new ParrotSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new PhantomSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new TamableSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new SheepSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new EndermanSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new EnderDragonSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new EvokerSynchronizer());
		EntitySynchronizerRegistry.addEntitySynchronizer(new MobSynchronizer());
				
		UglyHackThatDoesntWork.thisisstupid = (gameProfile, world) ->
		{
			AdvancedAbstractClientPlayerEntity entity = new AdvancedAbstractClientPlayerEntity((ClientLevel) world, gameProfile);
			
			Minecraft.getInstance().getSkinManager().registerSkins(gameProfile, (type, resourceLocation, texture) -> 
			{				
				if(type == Type.CAPE)
				{
					entity.capeResourceLocation = Minecraft.getInstance().getSkinManager().registerTexture(texture, type);
				}
				else if(type == Type.SKIN)
				{
					entity.skinResourceLocation = Minecraft.getInstance().getSkinManager().registerTexture(texture, type);
					
					String modelName = texture.getMetadata("model");
					
					entity.modelName = modelName == null ? "default" : modelName;
				}
				else if(type == Type.ELYTRA)
				{
					entity.elytraResourceLocation = Minecraft.getInstance().getSkinManager().registerTexture(texture, type);
				}
			}, true);
			
			return entity;
		};
	}

	@EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
	public static class ClientEvents
	{
		
		@SubscribeEvent
		public static void onPlayerNotInWorld(ClientPlayerNetworkEvent.LoggedOutEvent event)
		{
			// Unregister every ability when leaving world
			if(event.getPlayer() != null)
			{
				BMorphMod.DYNAMIC_ABILITY_REGISTRY.unregisterAll();
				BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.unregisterAll();
				BMorphMod.VISUAL_MORPH_DATA.clear();
			}
		}
	}
	
//	@EventBusSubscriber(bus = Bus.FORGE)
//	public static class FovHandler
//	{
//		@SubscribeEvent
//		public static void onFOVUpdated(final FOVUpdateEvent event)
//		{
//			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
//			
//			if(cap.isPresent())
//			{
//				IMorphCapability resolved = cap.resolve().get();
//				
//				if(resolved.hasAbility(AbilityRegistry.SWIFTNESS_ABILITY.get()) || resolved.hasAbility(AbilityRegistry.EXTREME_SWIFTNESS_ABILITY.get()))
//				{
//					event.setNewfov((float) ((Minecraft.getInstance().gameSettings.fov / 100f) * (event.getEntity().isSprinting() ? 1.17f : 1)));
////					event.setNewfov(-2);
//				}
//			}
//		}
//	}
}
