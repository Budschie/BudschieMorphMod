package de.budschie.bmorph.main;

import org.lwjgl.glfw.GLFW;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import de.budschie.bmorph.entity.EntityRegistry;
import de.budschie.bmorph.entity.rendering.MorphEntityRenderer;
import de.budschie.bmorph.morph.AdvancedAbstractClientPlayerEntity;
import de.budschie.bmorph.morph.UglyHackThatDoesntWork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
public class ClientSetup
{
	public static final KeyBinding TOGGLE_MORPH_UI = new KeyBinding("key.bmorph.toggle_morph", GLFW.GLFW_KEY_SEMICOLON, "key.bmorph.bmorph_category");
	public static final KeyBinding SCROLL_UP_MORPH_UI = new KeyBinding("key.bmorph.scroll_up", GLFW.GLFW_KEY_UP, "key.bmorph.bmorph_category");
	public static final KeyBinding SCROLL_DOWN_MORPH_UI = new KeyBinding("key.bmorph.scroll_down", GLFW.GLFW_KEY_DOWN, "key.bmorph.bmorph_category");
	
	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event)
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.MORPH_ENTITY.get(), manager -> new MorphEntityRenderer(manager));
		ClientRegistry.registerKeyBinding(TOGGLE_MORPH_UI);
		ClientRegistry.registerKeyBinding(SCROLL_UP_MORPH_UI);
		ClientRegistry.registerKeyBinding(SCROLL_DOWN_MORPH_UI);
		
		UglyHackThatDoesntWork.thisisstupid = (gameProfile, world) ->
		{
			System.out.println(gameProfile.getId());
			
			AdvancedAbstractClientPlayerEntity entity = new AdvancedAbstractClientPlayerEntity((ClientWorld) world, gameProfile);
			
			Minecraft.getInstance().getSkinManager().loadProfileTextures(gameProfile, (type, resourceLocation, texture) -> 
			{
				System.out.println("Downloading texture with hash " + texture.getHash());
				System.out.println("URL: " + texture.getUrl());
				
				if(type == Type.CAPE)
				{
					entity.capeResourceLocation = Minecraft.getInstance().getSkinManager().loadSkin(texture, type);;
				}
				else if(type == Type.SKIN)
				{
					entity.skinResourceLocation = Minecraft.getInstance().getSkinManager().loadSkin(texture, type);
				}
				else if(type == Type.ELYTRA)
				{
					entity.elytraResourceLocation = Minecraft.getInstance().getSkinManager().loadSkin(texture, type);
				}
			}, true);
			
			return entity;
		};
	}
}
