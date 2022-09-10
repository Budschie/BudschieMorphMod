package de.budschie.bmorph.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.VisualMorphDataRegistry.VisualMorphData;
import de.budschie.bmorph.util.Pair;
import de.budschie.bmorph.util.UiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// The current state of this class...
// TODO: Ability logos; maybe copy attributes of morph to the player
public class FilteredSimpleMorphGui extends AbstractMorphGui
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private ArrayList<MorphWidget> morphWidgets;
	private int scroll = 0;
	private int horizontalScroll = 0;
	// private BiFunction<IMorphCapability, List<MorphItem>, List<MorphItem>> filter;
	BiPredicate<IMorphCapability, Integer> filter;
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	/** Deprecated. I cannot provide a smooth deprecation transition here because of type erasure and ambiguity of arguments. **/
	public FilteredSimpleMorphGui(ResourceLocation morphGuiTypeIcon, String unlocalizedGuiType, BiPredicate<IMorphCapability, Integer> filter)
	{
		super(morphGuiTypeIcon, unlocalizedGuiType);
		
		this.filter = filter;
		this.morphWidgets = new ArrayList<>();
	}
	
	@Override
	public void showGui()
	{
		@SuppressWarnings("resource")
		LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
	
			// Create a list of indices of morphs
			List<Integer> morphList = new ArrayList<>();
			
			// This is dumb
			for(int i = 0; i < resolved.getMorphList().getMorphArrayList().size(); i++)
			{
				if(filter.test(resolved, i))
					morphList.add(i);
			}
			
			// Nice
			morphWidgets.add(new MorphWidget(null, false, -1, 69));
			
			HashMap<EntityType<?>, Pair<MorphWidget, Integer>> currentWidgetHeads = new HashMap<>();
			
			for(int i = 0; i < morphList.size(); i++)
			{
				int indexOfMorph = morphList.get(i);
				
				MorphItem item = resolved.getMorphList().getMorphArrayList().get(indexOfMorph);
				
				float morphScale = 1;
				VisualMorphData visualData = BMorphMod.VISUAL_MORPH_DATA.getDataForMorph(item);
				
				if(visualData != null)
					morphScale = visualData.getScale();
				
				MorphWidget widget = new MorphWidget(item, resolved.getFavouriteList().containsMorphItem(item), indexOfMorph, morphScale);
				
				Pair<MorphWidget, Integer> currentWidgetHead = currentWidgetHeads.get(widget.morphItem.getEntityType());
				
				// Check if there is a head.
				if(currentWidgetHead != null)
				{
					// There is a head, add to head
					MorphWidget head = currentWidgetHead.getA();
					head.child = widget;
				}
				else
				{
					// No head, add to widget list
					morphWidgets.add(widget);
				}
				
				// Set as new entity head
				currentWidgetHeads.put(widget.morphItem.getEntityType(), new Pair<>(widget, currentWidgetHead == null ? 0 : currentWidgetHead.getB() + 1));
			}
			
			for(int i = 1; i < morphWidgets.size(); i++)
			{
				MorphWidget widget = morphWidgets.get(i);
				Pair<MorphWidget, Integer> currentWidgetHead = currentWidgetHeads.get(widget.morphItem.getEntityType());
				widget.depth = currentWidgetHead.getB();
			}			
		}
		
		this.checkScroll();
	}
	
	@Override
	public void hideGui()
	{
		morphWidgets = new ArrayList<>();
	}
	
	/** This method iterates over every widget element and checks if the favourite status has changed. **/
	private void updateGui()
	{
		LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			
			for(int i = 1; i < morphWidgets.size(); i++)
			{
				MorphWidget parent = morphWidgets.get(i);
				
				// Iterate over itself and every child of the parent
				for(MorphWidget child : parent)
					child.isFavourite = resolved.getFavouriteList().containsMorphItem(child.morphItem);
			}
		}
	}
	
	@Override
	public void onFavouriteChanged()
	{
		// Reload every GUI element
		updateGui();
	}
	
	@Override
	public void renderWidgets(PoseStack matrixStack, float partialTicks)
	{							
		int startY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - MorphWidget.getHeight() / 2
				- scroll * MorphWidget.getHeight();
		int advanceY = 0;

		// First, we divide the gui scaled width by the morph widgets. This gives us the total widgets that could possibly fit onto the screen.
		// Then we half the result so that we get the amount of widgets that would fit on half of the screen.
		// Then, we look if the current scroll value exceedes this amount, and if it does, it should increase the scroll offset.
		// This leads to a scroll if we are exceeding the half of the screen.
		int scrollOffX = (int)(Math.ceil(Math.max(0, horizontalScroll - ((Minecraft.getInstance().getWindow().getGuiScaledWidth()) / ((float)MorphWidget.getWidth())) * 0.5)) * MorphWidget.getWidth());
		
		for (int i = 0; i < morphWidgets.size(); i++)
		{
			if((startY + advanceY + MorphWidget.getHeight()) > 0 && (startY + advanceY) < Minecraft.getInstance().getWindow().getGuiScaledHeight())
			{
				// rendered++;
				
				MorphWidget widget = morphWidgets.get(i);
				matrixStack.pushPose();
				matrixStack.translate(6 - scrollOffX, startY + advanceY, 0);
				widget.render(matrixStack, i == scroll, horizontalScroll, partialTicks);
	//			Minecraft.getInstance().fontRenderer.drawText(matrixStack, new StringTextComponent("Index " + i), 0, 0,
	//					0xffffff);
				matrixStack.popPose();
			}
			advanceY += MorphWidget.getHeight();
		}
	}
	
	@Override
	public int getMorphIndex()
	{
		return morphWidgets.get(scroll).traverse(horizontalScroll).morphListIndex;
	}
	
	@Override
	public void scroll(int amount)
	{
		scroll += amount;
		checkScroll();
	}
	
	@Override
	public void horizontalScroll(int scroll)
	{
		int depth = morphWidgets.get(this.scroll).depth + 1;
		this.horizontalScroll = depth == 0 ? 0 : (horizontalScroll + scroll) % depth;
		
		if(this.horizontalScroll < 0)
			this.horizontalScroll = depth + horizontalScroll;
	}
	
	private void checkScroll()
	{
		scroll = Math.max(Math.min(scroll, morphWidgets.size() - 1), 0);
		horizontalScroll(0);
	}
	
	@Override
	public void setScroll(int scroll)
	{
		// Remap from -1 to x to 0 to x.
		this.scroll = scroll < 0 ? -1 : scroll + 1;
		checkScroll();
	}
	
	@Override
	public MorphItem getMorphItem()
	{
		return this.scroll < 0 ? null : morphWidgets.get(scroll).traverse(horizontalScroll).morphItem;
	}
	
	public ArrayList<MorphWidget> getMorphWidgets()
	{
		return morphWidgets;
	}
	
	// This class represents one morph entry on the side of the screen
	@EventBusSubscriber(value = Dist.CLIENT)
	public static class MorphWidget implements Iterable<MorphWidget>
	{		
		public static final int WIDGET_WIDTH = 48;
		public static final int WIDGET_HEIGHT = 64;
		public static final double SCALE_FACTOR = 1.3;
		
		public static final float ENTITY_SCALE_FACTOR = 30;
		
		public static final Quaternion ENTITY_ROTATION = new Quaternion(10, 45, 0, true);
		public static final Quaternion NAMEPLATE_ORIENTATION = new Quaternion(0, 180 - 45, 0, true);
		
		
		private static final ResourceLocation MORPH_WINDOW_NORMAL = new ResourceLocation(References.MODID, "textures/gui/morph_window_normal.png");
		private static final ResourceLocation MORPH_WINDOW_SELECTED = new ResourceLocation(References.MODID, "textures/gui/morph_window_selected.png");
		private static final ResourceLocation DEMORPH = new ResourceLocation(References.MODID, "textures/gui/demorph.png");
		private static final ResourceLocation FAVOURITE = new ResourceLocation(References.MODID, "textures/gui/favourite_star.png");
		
		private static Entity dumbFix = null;
		
		Optional<Entity> morphEntity = Optional.empty();
		MorphItem morphItem;
		MorphWidget child;
		boolean isFavourite;
		float morphScale;
		// This is dumb
		int depth;
		boolean crashed = false;
		
		int morphListIndex;
		
		public MorphWidget(MorphItem morphItem, boolean isFavourite, int morphListIndex, float morphScale)
		{
			this.morphItem = morphItem;
			this.isFavourite = isFavourite;
			this.morphListIndex = morphListIndex;
			this.morphScale = morphScale;
		}
		
		@SubscribeEvent
		public static void onRenderingNameplate(RenderNameplateEvent event)
		{
			if(dumbFix != null && event.getEntity() == dumbFix && event.getEntity().hasCustomName())
			{
				event.getPoseStack().mulPose(NAMEPLATE_ORIENTATION);
				event.setResult(Result.ALLOW);
			}
		}
		
		// This is kinda dumb
		public void render(PoseStack stack, boolean isSelected, int horizontalScroll, float partialTicks)
		{
			render(stack, isSelected, horizontalScroll, 0, partialTicks);
		}
		
		public void render(PoseStack stack, boolean isSelected, int horizontalScroll, int currentDepth, float partialTicks)
		{
			RenderSystem.enableBlend();
			RenderSystem.setShaderTexture(0, (isSelected && currentDepth == horizontalScroll) ? MORPH_WINDOW_SELECTED : MORPH_WINDOW_NORMAL);
			GuiComponent.blit(stack, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
			
			// Draw entity logic
			if(morphItem == null)
			{
				RenderSystem.setShaderTexture(0, DEMORPH);
				GuiComponent.blit(stack, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
			}
			else
			{
				if(!morphEntity.isPresent() && !crashed)
				{
					try
					{
						morphEntity = Optional.of(morphItem.createEntity(Minecraft.getInstance().level));
					}
					catch(NullPointerException ex)
					{
						crashed = true;
						LOGGER.catching(ex);
						LOGGER.warn("Could not render entity ", morphItem.getEntityType().getRegistryName().toString() + ".");
					}
				}
				
				if(morphEntity.isPresent())
				{
				    MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
				    			    
					stack.pushPose();
					stack.translate(30, 70, 50);
					stack.scale(ENTITY_SCALE_FACTOR * morphScale, -ENTITY_SCALE_FACTOR * morphScale, ENTITY_SCALE_FACTOR * morphScale);
					stack.mulPose(ENTITY_ROTATION);
					
					dumbFix = this.morphEntity.get();
					
					Minecraft.getInstance().getEntityRenderDispatcher().overrideCameraOrientation(new Quaternion(0, 0, 0, false));
					
					// We have to set the position or else name tags won't get rendered because there is a distance check
					BlockPos position = Minecraft.getInstance().getEntityRenderDispatcher().camera.getBlockPosition();
					this.morphEntity.get().setPos(position.getX(), position.getY(), position.getZ());
					
					// Note: Entity nameplate doesn't get rendered because the distance is too high.
					Minecraft.getInstance().getEntityRenderDispatcher().render(morphEntity.get(), 0, 0, 0, 0, 0, stack, buffer, 15728880);
					
					dumbFix = null;
					
					buffer.endBatch();
					
					stack.popPose();
				}
				
				if(morphItem.isDisabled())
				{
					float calculatedHeight = getHeight() * morphItem.getDisabledProgress(partialTicks);
					
					// F this conversion bullshit with the ARGB format
					// Also I think I might have trial-errored too much here but who cares
					UiUtils.drawColoredRectangle(stack.last().pose(), 0, getHeight(), getWidth(), -calculatedHeight + getHeight(), 0, 0, 0, 0.8f);
				}
			}
			
			if(isFavourite)
			{
				RenderSystem.setShaderTexture(0, FAVOURITE);
				GuiComponent.blit(stack, 7, 7, 0, 0, 16, 16, 16, 16);
			}
			
			// Check if we have a child
			if(child != null)
			{
				stack.pushPose();
				stack.translate(getWidth(), 0, 0);
				child.render(stack, isSelected, horizontalScroll, currentDepth+1, partialTicks);
				stack.popPose();
			}
		}
		
		/** This method will return the xth child of this widget. **/
		public MorphWidget traverse(int amount)
		{
			MorphWidget currentWidget = this;
			
			for(int i = 0; i < amount; i++)
			{
				currentWidget = currentWidget.child;
			}
			
			return currentWidget;
		}
		
		public int getDepth()
		{
			return depth;
		}
		
		public static int getHeight()
		{
			return (int) (WIDGET_HEIGHT * SCALE_FACTOR);
		}
		
		public static int getWidth()
		{
			return (int) (WIDGET_WIDTH * SCALE_FACTOR);
		}

		@Override
		public Iterator<MorphWidget> iterator()
		{
			return new MorphWidgetIterator(this);
		}
	}
	
	public static class MorphWidgetIterator implements Iterator<MorphWidget>
	{
		private MorphWidget head;
		
		public MorphWidgetIterator(MorphWidget head)
		{
			this.head = head;
		}

		@Override
		public boolean hasNext()
		{
			return head != null;
		}

		@Override
		public MorphWidget next()
		{
			MorphWidget current = head;
			head = head.child;
			return current;
		}
	}
}
