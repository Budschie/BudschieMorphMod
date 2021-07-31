package de.budschie.bmorph.gui;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ClientSetup;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphRequestAbilityUsage;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

// Here happens all the client stuff. The name is fake
@EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class MorphGuiHandler
{
	private static boolean toggle = false;
	
	private static ArrayList<EyeHeightChangePair> scheduledChanges = new ArrayList<>();
	
	public static void toggle()
	{
		toggle = !toggle;
		
		if(toggle)
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			cap.ifPresent(resolved -> resolved.getCurrentMorphIndex().ifPresent(index -> NewMorphGui.setScroll(index + 1)));
			
			NewMorphGui.showGui();
		}
		else
			NewMorphGui.hideGui();
	}
	
	@SubscribeEvent
	public static void onPressedKey(ClientTickEvent event)
	{		
		if(Minecraft.getInstance().world != null)
		{
			Minecraft.getInstance().world.getPlayers().forEach(player ->
			{
				LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					IMorphCapability resolved = cap.resolve().get();
					
					if(resolved.hasAbility(AbilityRegistry.CLIMBING_ABILITY.get()) && player.collidedHorizontally && !player.abilities.isFlying)
					{
						Vector3d toSet = player.getMotion().add(0, .2f, 0);
						player.setMotion(new Vector3d(toSet.x, Math.min(toSet.y, .2f), toSet.z));
					}
					if(resolved.hasAbility(AbilityRegistry.WATER_BREATHING_ABILITY.get()) && player.isInWater())
						player.setAir(14 * 20);
				}
			});
			
			if(ClientSetup.TOGGLE_MORPH_UI.isPressed())
				toggle();
			
			if(ClientSetup.USE_ABILITY_KEY.isPressed())
				MainNetworkChannel.INSTANCE.sendToServer(new MorphRequestAbilityUsage.MorphRequestAbilityUsagePacket());
			
			if(toggle && ClientSetup.SCROLL_DOWN_MORPH_UI.isPressed())
				NewMorphGui.scroll(1);
			
			if(toggle && ClientSetup.SCROLL_UP_MORPH_UI.isPressed())
				NewMorphGui.scroll(-1);
		}
	}
	
	@SubscribeEvent
	public static void onPressedKeyboardKeyRaw(KeyInputEvent event)
	{
		if(toggle && ClientSetup.MORPH_UI.isPressed())
		{
			toggle();
			MainNetworkChannel.INSTANCE.sendToServer(new RequestMorphIndexChangePacket(NewMorphGui.getScroll() - 1));
		}
	}
	
	@SubscribeEvent
	public static void onRenderOverlayEvent(RenderGameOverlayEvent.Post event)
	{
		if(toggle && event.getType() == ElementType.TEXT)
		{
			// MorphGui.render(event.getMatrixStack());
			NewMorphGui.render(event.getMatrixStack());
		}
	}
	
	public static void scheduleEyeHeightChange(float newEyeHeight, PlayerEntity player)
	{
		EyeHeightChangePair pair = new EyeHeightChangePair();
		pair.newEyeHeight = newEyeHeight;
		pair.player = player;
		scheduledChanges.add(pair);
	}
	
	// Yeah im not going to create a non-default constructor here as i am to lazy...
	private static class EyeHeightChangePair
	{
		float newEyeHeight;
		PlayerEntity player;
	}
}
