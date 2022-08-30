package de.budschie.bmorph.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class AbstractMorphGui implements IForgeRegistryEntry<AbstractMorphGui>
{
	private ResourceLocation id;
	
	private final ResourceLocation morphGuiTypeIcon;
	private final String unlocalizedGuiType;
	
	/** 
	 * This is the constructor of the abstract class {@code AbstractMorphGui}. It has following parameters:
	 * @param morphGuiTypeIcon This is the resource location of the icon. See {@link AbstractMorphGui#getMorphGuiTypeIcon()} for more details.
	 * @param unlocalizedGuiType This is the string that represents the unlocalized name of the gui type. See {@link AbstractMorphGui#getUnlocalizedGuiType()} for more information.
	**/
	public AbstractMorphGui(ResourceLocation morphGuiTypeIcon, String unlocalizedGuiType)
	{
		this.morphGuiTypeIcon = morphGuiTypeIcon;
		this.unlocalizedGuiType = unlocalizedGuiType;
	}
	
	/** Every time the gui is being hidden, this method may be called to properly handle such behaviour. **/
	public abstract void hideGui();
	
	/** Every time before the gui is being shown, this method may be called to properly handle such behaviour. **/
	public abstract void showGui();
	
	/** This method is used to render everything related to this gui. **/
	public void render(PoseStack matrixStack, float partialTicks)
	{
		renderWidgets(matrixStack, partialTicks);
	}
	
	public abstract void renderWidgets(PoseStack matrixStack, float partialTicks);
	
	/** Scrolls around in the morph menu. When scrolling up, {@code amount} is negative, when scrolling down, {@code amount} is positive. **/
	public abstract void scroll(int amount);
	
	/**
	 *  This method returns the morph index.
	 * @return An integer representing the morph index. Any negative number shall represent a demorph, everything else represents a morph.
	**/
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public abstract int getMorphIndex();
	
	/**
	 * This method shall return the currently selected morph item, or null if there isn't any selection.
	 */
	public abstract MorphItem getMorphItem();
	
	/** This method sets the scroll of the morph gui. Any negative number represents a demorph, everything above -1 represents an index of a morph. **/
	public abstract void setScroll(int scroll);
	
	/** This scrolls horizontally. **/
	public abstract void horizontalScroll(int scroll);
	
	/** Returns the resource location of the icon of the morph gui. This will be displayed on the top of the gui when rendering the morph guis. **/
	public ResourceLocation getMorphGuiTypeIcon()
	{
		return morphGuiTypeIcon;
	}
	
	/** Returns the <italic>unlocalized</italic> name of the gui type. An example would be {@code morph_gui.bmorph.simple}. This will be displayed on the right of the gui icon. **/
	public String getUnlocalizedGuiType()
	{
		return unlocalizedGuiType;
	}
	
	public void onFavouriteChanged()
	{
		
	}
	
	@Override
	public AbstractMorphGui setRegistryName(ResourceLocation name)
	{
		this.id = name;
		return this;
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return id;
	}
	
	@Override
	public Class<AbstractMorphGui> getRegistryType()
	{
		return AbstractMorphGui.class;
	}
}
