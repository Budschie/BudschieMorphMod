package de.budschie.bmorph.gui;

import java.util.ArrayList;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;

public class MorphGui
{	
	private static final ResourceLocation MORPH_WINDOW_NORMAL = new ResourceLocation(References.MODID, "textures/gui/morph_window_normal.png");
	private static final ResourceLocation MORPH_WINDOW_SELECTED = new ResourceLocation(References.MODID, "textures/gui/morph_window_selected.png");
	private static final ResourceLocation DEMORPH = new ResourceLocation(References.MODID, "textures/gui/demorph.png");
	
	private static int currentScroll = 0;
	
	public static void render(MatrixStack stack)
	{		
		LazyOptional<IMorphCapability> morphs = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(morphs.isPresent())
		{
			ArrayList<MorphItem> morphList = morphs.resolve().get().getMorphList().getMorphArrayList();
			
			int scaledWidth = Minecraft.getInstance().getMainWindow().getScaledWidth();
			int scaledHeight = Minecraft.getInstance().getMainWindow().getScaledHeight();
					
			int amountOfRectanglesVertical = (int) Math.ceil(scaledHeight / 64f);
			currentScroll = Math.max(Math.min(currentScroll, morphList.size()), 0);
			
			//float offset = currentScroll - Math.min(amountOfRectanglesVertical / 2, morphList.size());
			float offset = amountOfRectanglesVertical / 2 - currentScroll - .75f;
								
			//AbstractGui.blit(stack, 1, (int) (((offset < 0 && !(offset > (-morphList.size()))) ? offset % 1 : offset) * (64)), 48, scaledHeight + 64, 0, 0, 48, scaledHeight + 64, 48, 64);
			
			int fromIndex = Math.max(0, (int) Math.floor(-offset)), toIndex = (int) Math.min(amountOfRectanglesVertical + Math.ceil(-offset), morphList.size() + 1);
			
			for(int i = fromIndex; i < toIndex; i++)
			{
				if(i == currentScroll)
					Minecraft.getInstance().getTextureManager().bindTexture(MORPH_WINDOW_SELECTED);
				else
					Minecraft.getInstance().getTextureManager().bindTexture(MORPH_WINDOW_NORMAL);
				
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				
				AbstractGui.blit(stack, 5, (int) ((i + offset) * 64), 0, 0, 0, 48, 64, 64, 48);
				
				if(i == 0)
				{
					Minecraft.getInstance().getTextureManager().bindTexture(DEMORPH);
					AbstractGui.blit(stack, 5, (int) ((i + offset) * 64), 0, 0, 0, 48, 64, 64, 48);
				}
				else
				{
					Entity thiscouldintheorybecachedbutiamtolazytodothat = morphList.get(i - 1).createEntity(Minecraft.getInstance().world);
					
					InventoryScreen.drawEntityOnScreen(24, (int) (59 + (i + offset) * 64), 20, -70, 30, (LivingEntity) thiscouldintheorybecachedbutiamtolazytodothat);
				}
			}
			
			RenderSystem.enableBlend();
			
//			RenderSystem.
//			RenderSystem.enableBlend();
//			RenderSystem.defaultBlendFunc();
		}
	}
	
	public static void scrollUp()
	{
		// I hate MC UI code
		currentScroll--;
	}
	
	public static void scrollDown()
	{
		currentScroll++;
	}
	
	public static int getCurrentScroll()
	{
		return currentScroll;
	}
	
	public static void setCurrentScroll(int currentScroll)
	{
		MorphGui.currentScroll = currentScroll;
	}
}
