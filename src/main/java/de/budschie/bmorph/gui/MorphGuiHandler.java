package de.budschie.bmorph.gui;

import org.lwjgl.glfw.GLFW;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ClientSetup;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class MorphGuiHandler
{
	private static boolean toggle = false;
	
	public static void toggle()
	{
		toggle = !toggle;
		
		if(toggle)
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			cap.ifPresent(resolved -> resolved.getCurrentMorphIndex().ifPresent(index -> MorphGui.setCurrentScroll(index)));
		}
	}
	
	@SubscribeEvent
	public static void onPressedKey(ClientTickEvent event)
	{
		if(ClientSetup.TOGGLE_MORPH_UI.isPressed())
			toggle();
		
		if(toggle && ClientSetup.SCROLL_DOWN_MORPH_UI.isPressed())
			MorphGui.scrollDown();
		
		if(toggle && ClientSetup.SCROLL_UP_MORPH_UI.isPressed())
			MorphGui.scrollUp();
	}
	
	@SubscribeEvent
	public static void onPressedKeyboardKeyRaw(KeyInputEvent event)
	{
		if(toggle && event.getKey() == GLFW.GLFW_KEY_ENTER && event.getAction() == GLFW.GLFW_PRESS)
		{
			toggle = false;
			MainNetworkChannel.INSTANCE.sendToServer(new RequestMorphIndexChangePacket(MorphGui.getCurrentScroll()));
		}
	}
	
	@SubscribeEvent
	public static void onRenderOverlayEvent(RenderGameOverlayEvent.Post event)
	{
		if(toggle)
		{
			MorphGui.render(event.getMatrixStack());
		}
	}
}
