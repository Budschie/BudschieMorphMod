package de.budschie.bmorph.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ClientSetup;
import de.budschie.bmorph.morph.FavouriteNetworkingHelper;
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
	private static int currentIndex = 0;
	
	private static Optional<AbstractMorphGui> currentMorphGui = Optional.empty();
	private static boolean guiHidden = true;
	
	private static ArrayList<EyeHeightChangePair> scheduledChanges = new ArrayList<>();
	
	public static void traverseToIndexAndSetGui()
	{
		Iterator<AbstractMorphGui> iterator = MorphGuiRegistry.REGISTRY.get().getValues().iterator();
		
		for(int i = 0; i < currentIndex; i++)
		{
			iterator.next();
		}
		
		// This feels stupid...
		currentMorphGui = Optional.of(iterator.next());
	}
	
	/** This method hides the previous morph UI, traverses, and then shows the new morph UI **/
	private static void updateCurrentMorphUI()
	{
		currentMorphGui.ifPresent(ui -> ui.hideGui());
		
		traverseToIndexAndSetGui();
		
		currentMorphGui.get().showGui();
	}
	
	public static Optional<AbstractMorphGui> getCurrentMorphGui()
	{
		return currentMorphGui;
	}
	
	public static void showGui()
	{
		guiHidden = false;
		currentMorphGui.ifPresent(gui -> gui.showGui());
	}
	
	public static void hideGui()
	{
		guiHidden = true;
		currentMorphGui.ifPresent(gui -> gui.hideGui());
	}
	
	public static boolean canGuiBeDisplayed()
	{
		return !guiHidden && currentMorphGui.isPresent();
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
					
					// Initiate climbing...
					if(resolved.hasAbility(AbilityRegistry.CLIMBING_ABILITY.get()) && player.collidedHorizontally && !player.abilities.isFlying)
					{
						Vector3d toSet = player.getMotion().add(0, .2f, 0);
						player.setMotion(new Vector3d(toSet.x, Math.min(toSet.y, .2f), toSet.z));
					}
					
					// Updating the bubble GUI on the client so that the bubbles are always full
					if(resolved.hasAbility(AbilityRegistry.WATER_BREATHING_ABILITY.get()) && player.isInWater())
						player.setAir(14 * 20);
					
					if(resolved.hasAbility(AbilityRegistry.FLY_ABILITY.get()))
						player.abilities.allowFlying = true;
				}
			});
			
			if(!currentMorphGui.isPresent())
				traverseToIndexAndSetGui();
			
			if(ClientSetup.TOGGLE_MORPH_UI.isPressed())
			{				
				if (guiHidden)
					showGui();
				else
					hideGui();
			}
			
			if(canGuiBeDisplayed())
			{
				if (ClientSetup.SCROLL_DOWN_MORPH_UI.isPressed())
					currentMorphGui.get().scroll(1);
	
				if (ClientSetup.SCROLL_UP_MORPH_UI.isPressed())
					currentMorphGui.get().scroll(-1);
				
				if (ClientSetup.SCROLL_LEFT_MORPH_UI.isPressed())
					currentMorphGui.get().horizontalScroll(-1);
	
				if (ClientSetup.SCROLL_RIGHT_MORPH_UI.isPressed())
					currentMorphGui.get().horizontalScroll(1);

				
				if(ClientSetup.NEXT_MORPH_UI.isPressed())
				{
					currentIndex++;
					currentIndex %= MorphGuiRegistry.REGISTRY.get().getValues().size();
					traverseToIndexAndSetGui();
					updateCurrentMorphUI();
				}
				
				if(ClientSetup.PREVIOUS_MORPH_UI.isPressed())
				{
					currentIndex--;
					currentIndex %= MorphGuiRegistry.REGISTRY.get().getValues().size();
					traverseToIndexAndSetGui();
					updateCurrentMorphUI();
				}
				
				if(ClientSetup.TOGGLE_MORPH_FAVOURITE.isPressed())
				{
					LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
					
					if(cap.isPresent())
					{
						IMorphCapability resolved = cap.resolve().get();
						int favouriteMorphIndex = currentMorphGui.get().getMorphIndex();
						
						if(favouriteMorphIndex < 0)
							System.out.println("Yo wat");
						else
						{
							if(resolved.getFavouriteList().containsMorphItem(resolved.getMorphList().getMorphArrayList().get(favouriteMorphIndex)))
								FavouriteNetworkingHelper.removeFavouriteMorph(favouriteMorphIndex);
							else
								FavouriteNetworkingHelper.addFavouriteMorph(favouriteMorphIndex);
						}
					}
					
					currentMorphGui.ifPresent(morphGui -> morphGui.onFavouriteChanged());
				}
			}
			
			if(ClientSetup.USE_ABILITY_KEY.isPressed())
				MainNetworkChannel.INSTANCE.sendToServer(new MorphRequestAbilityUsage.MorphRequestAbilityUsagePacket());			
		}
	}
	
	@SubscribeEvent
	public static void onPressedKeyboardKeyRaw(KeyInputEvent event)
	{
		if(canGuiBeDisplayed() && ClientSetup.MORPH_UI.isPressed() && currentMorphGui.isPresent())
		{
			MainNetworkChannel.INSTANCE.sendToServer(new RequestMorphIndexChangePacket(currentMorphGui.get().getMorphIndex()));
			
			if(guiHidden)
				showGui();
			else
				hideGui();			
		}
	}
	
	@SubscribeEvent
	public static void onRenderOverlayEvent(RenderGameOverlayEvent.Post event)
	{
		if(canGuiBeDisplayed() && event.getType() == ElementType.TEXT && currentMorphGui.isPresent())
		{
			currentMorphGui.get().render(event.getMatrixStack());
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
