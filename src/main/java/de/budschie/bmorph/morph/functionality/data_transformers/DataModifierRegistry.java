package de.budschie.bmorph.morph.functionality.data_transformers;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class DataModifierRegistry
{
	public static final ResourceLocation DATA_MODIFIER_REGISTRY_ID = new ResourceLocation(References.MODID, "data_modifiers");
	
	public static DeferredRegister<DataModifierHolder<? extends DataModifier>> DATA_MODIFIER_REGISTRY = DeferredRegister.<DataModifierHolder<? extends DataModifier>>create(DATA_MODIFIER_REGISTRY_ID, References.MODID); 
	
	@SuppressWarnings("unchecked")
	public static Supplier<IForgeRegistry<DataModifierHolder<? extends DataModifier>>> REGISTRY = DATA_MODIFIER_REGISTRY.<DataModifierHolder<? extends DataModifier>>makeRegistry((Class<DataModifierHolder<? extends DataModifier>>)((Class<?>)DataModifierHolder.class), () -> new RegistryBuilder<DataModifierHolder<?>>().disableSaving().setName(DATA_MODIFIER_REGISTRY_ID));
	
	public static final RegistryObject<DataModifierHolder<ConditionalModifier>> CONDITIONAL_MODIFIER = DATA_MODIFIER_REGISTRY.register("conditional_modifier", () -> new DataModifierHolder<>(ConditionalModifier.CODEC));
	public static final RegistryObject<DataModifierHolder<SetIfPresentModifier>> SET_IF_PRESENT_MODIFIER = DATA_MODIFIER_REGISTRY.register("set_if_present", () -> new DataModifierHolder<>(SetIfPresentModifier.CODEC));
}

