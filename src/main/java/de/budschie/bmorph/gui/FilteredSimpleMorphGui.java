package de.budschie.bmorph.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.common.util.LazyOptional;

// The current state of this class...
public class FilteredSimpleMorphGui extends AbstractMorphGui
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private ArrayList<MorphWidget> morphWidgets;
	private int scroll = 0;
	private int horizontalScroll = 0;
	// private BiFunction<IMorphCapability, List<MorphItem>, List<MorphItem>> filter;
	BiPredicate<IMorphCapability, Integer> filter;
	
	public FilteredSimpleMorphGui(ResourceLocation morphGuiTypeIcon, String unlocalizedGuiType, BiPredicate<IMorphCapability, Integer> filter)
	{
		super(morphGuiTypeIcon, unlocalizedGuiType);
		
		this.filter = filter;
		this.morphWidgets = new ArrayList<>();
	}
	
	@Override
	public void showGui()
	{
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
			
			morphWidgets.add(new MorphWidget(null, false, -1));
			
			HashMap<EntityType<?>, Pair<MorphWidget, Integer>> currentWidgetHeads = new HashMap<>();
			
			for(int i = 0; i < morphList.size(); i++)
			{
				int indexOfMorph = morphList.get(i);
				
				MorphItem item = resolved.getMorphList().getMorphArrayList().get(indexOfMorph);
				
				MorphWidget widget = new MorphWidget(item, resolved.getFavouriteList().containsMorphItem(item), indexOfMorph);
				
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
	public void renderWidgets(MatrixStack matrixStack)
	{							
		
		int startY = Minecraft.getInstance().getMainWindow().getScaledHeight() / 2 - MorphWidget.getHeight() / 2
				- scroll * MorphWidget.getHeight();
		int advanceY = 0;

				
		for (int i = 0; i < morphWidgets.size(); i++)
		{
			if((startY + advanceY + MorphWidget.getHeight()) > 0 && (startY + advanceY) < Minecraft.getInstance().getMainWindow().getScaledHeight())
			{
				// rendered++;
				
				MorphWidget widget = morphWidgets.get(i);
				matrixStack.push();
				matrixStack.translate(6, startY + advanceY, 0);
				widget.render(matrixStack, i == scroll, horizontalScroll);
	//			Minecraft.getInstance().fontRenderer.drawText(matrixStack, new StringTextComponent("Index " + i), 0, 0,
	//					0xffffff);
				matrixStack.pop();
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
	public static class MorphWidget implements Iterable<MorphWidget>
	{		
		public static final int WIDGET_WIDTH = 48;
		public static final int WIDGET_HEIGHT = 64;
		public static final double SCALE_FACTOR = 1.3;
		
		public static final float ENTITY_SCALE_FACTOR = 30;
		
		public static final Quaternion ENTITY_ROTATION = new Quaternion(10, 45, 0, true);
		
		private static final ResourceLocation MORPH_WINDOW_NORMAL = new ResourceLocation(References.MODID, "textures/gui/morph_window_normal.png");
		private static final ResourceLocation MORPH_WINDOW_SELECTED = new ResourceLocation(References.MODID, "textures/gui/morph_window_selected.png");
		private static final ResourceLocation DEMORPH = new ResourceLocation(References.MODID, "textures/gui/demorph.png");
		private static final ResourceLocation FAVOURITE = new ResourceLocation(References.MODID, "textures/gui/favourite_star.png");
		
		Optional<Entity> morphEntity = Optional.empty();
		MorphItem morphItem;
		MorphWidget child;
		boolean isFavourite;
		// This is dumb
		int depth;
		boolean crashed = false;
		
		int morphListIndex;
		
		public MorphWidget(MorphItem morphItem, boolean isFavourite, int morphListIndex)
		{
			this.morphItem = morphItem;
			this.isFavourite = isFavourite;
			this.morphListIndex = morphListIndex;
		}
		
		// This is kinda dumb
		public void render(MatrixStack stack, boolean isSelected, int horizontalScroll)
		{
			render(stack, isSelected, horizontalScroll, 0);
		}
		
		public void render(MatrixStack stack, boolean isSelected, int horizontalScroll, int currentDepth)
		{
			RenderSystem.enableBlend();
			Minecraft.getInstance().getTextureManager().bindTexture((isSelected && currentDepth == horizontalScroll) ? MORPH_WINDOW_SELECTED : MORPH_WINDOW_NORMAL);
			AbstractGui.blit(stack, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
			
			// Draw entity logic
			if(morphItem == null)
			{
				Minecraft.getInstance().getTextureManager().bindTexture(DEMORPH);
				AbstractGui.blit(stack, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
			}
			else
			{
				if(!morphEntity.isPresent() && !crashed)
				{
					try
					{
						morphEntity = Optional.of(morphItem.createEntity(Minecraft.getInstance().world));
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
				    IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
				    			    
					stack.push();
					stack.translate(30, 70, 50);
					stack.scale(ENTITY_SCALE_FACTOR, -ENTITY_SCALE_FACTOR, ENTITY_SCALE_FACTOR);
					stack.rotate(ENTITY_ROTATION);
					
					Minecraft.getInstance().getRenderManager().renderEntityStatic(morphEntity.get(), 0, 0, 0, 0, 0, stack, buffer, 15728880);
					
					buffer.finish();
					
					stack.pop();
				}
			}
			
			if(isFavourite)
			{
				Minecraft.getInstance().getTextureManager().bindTexture(FAVOURITE);
				AbstractGui.blit(stack, 7, 7, 0, 0, 16, 16, 16, 16);
			}
			
			// Check if we have a child
			if(child != null)
			{
				stack.push();
				stack.translate(getWidth(), 0, 0);
				child.render(stack, isSelected, horizontalScroll, currentDepth+1);
				stack.pop();
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
