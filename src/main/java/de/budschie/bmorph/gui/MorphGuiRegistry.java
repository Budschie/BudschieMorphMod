package de.budschie.bmorph.gui;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class MorphGuiRegistry
{
	public static DeferredRegister<AbstractMorphGui> MORPHGUI_REGISTRY = DeferredRegister.create(AbstractMorphGui.class, References.MODID); 
	public static Supplier<IForgeRegistry<AbstractMorphGui>> REGISTRY = MORPHGUI_REGISTRY.makeRegistry("morph_guis", () -> new RegistryBuilder<>());
}
