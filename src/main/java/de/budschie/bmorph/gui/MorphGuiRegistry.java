package de.budschie.bmorph.gui;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class MorphGuiRegistry
{
	public static final DeferredRegister<AbstractMorphGui> MORPH_GUI_REGISTRY = DeferredRegister.create(AbstractMorphGui.class, References.MODID); 
	public static final Supplier<IForgeRegistry<AbstractMorphGui>> REGISTRY = MORPH_GUI_REGISTRY.makeRegistry("morph_guis", () -> new RegistryBuilder<>());
	
	public static final RegistryObject<FilteredSimpleMorphGui> SIMPLE_MORPH_GUI = MORPH_GUI_REGISTRY.register("simple_default", () -> 
	new FilteredSimpleMorphGui(null, "morph_gui.bmorph.simple_default", (resolved, input) -> true));
	
	public static final RegistryObject<FilteredSimpleMorphGui> FAVOURITE_MORPH_GUI = MORPH_GUI_REGISTRY.register("simple_favourite", () -> 
	new FilteredSimpleMorphGui(null, "morph_gui.bmorph.simple_favourite", (resolved, input) -> resolved.getFavouriteList().containsMorphItem(resolved.getMorphList().getMorphArrayList().get(input))));
}
