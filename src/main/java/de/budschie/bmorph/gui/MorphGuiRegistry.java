package de.budschie.bmorph.gui;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class MorphGuiRegistry
{
	public static final ResourceLocation MORPH_GUI_REGISTRY_ID = new ResourceLocation(References.MODID, "morph_guis");
	
	public static final DeferredRegister<AbstractMorphGui> MORPH_GUI_REGISTRY = DeferredRegister.create(MORPH_GUI_REGISTRY_ID, References.MODID); 
	public static final Supplier<IForgeRegistry<AbstractMorphGui>> REGISTRY = MORPH_GUI_REGISTRY.makeRegistry(AbstractMorphGui.class, () -> new RegistryBuilder<AbstractMorphGui>().setName(MORPH_GUI_REGISTRY_ID));
	
	public static final RegistryObject<FilteredSimpleMorphGui> SIMPLE_MORPH_GUI = MORPH_GUI_REGISTRY.register("simple_default", () -> 
	new FilteredSimpleMorphGui(null, "morph_gui.bmorph.simple_default", (resolved, input) -> true));
	
	public static final RegistryObject<FilteredSimpleMorphGui> FAVOURITE_MORPH_GUI = MORPH_GUI_REGISTRY.register("simple_favourite", () -> 
	new FilteredSimpleMorphGui(null, "morph_gui.bmorph.simple_favourite", (resolved, input) -> resolved.getFavouriteList().containsMorphItem(resolved.getMorphList().getMorphArrayList().get(input))));
}
