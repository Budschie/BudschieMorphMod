package de.budschie.bmorph.morph;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class MorphReasonRegistry
{
	public static final ResourceLocation MORPH_REASON_REGISTRY_ID = new ResourceLocation(References.MODID, "morph_reasons");
	
	public static DeferredRegister<MorphReason> MORPH_REASON_REGISTRY = DeferredRegister.create(MORPH_REASON_REGISTRY_ID, References.MODID); 	
	public static Supplier<IForgeRegistry<MorphReason>> REGISTRY = MORPH_REASON_REGISTRY.makeRegistry(MorphReason.class, () -> new RegistryBuilder<MorphReason>().disableSaving().setName(MORPH_REASON_REGISTRY_ID));
	
	public static final RegistryObject<MorphReason> NONE = MORPH_REASON_REGISTRY.register("none", MorphReason::new);
	public static final RegistryObject<MorphReason> MORPHED_BY_ERROR = MORPH_REASON_REGISTRY.register("morphed_by_error", MorphReason::new);
	public static final RegistryObject<MorphReason> MORPHED_BY_UI = MORPH_REASON_REGISTRY.register("morphed_by_ui", MorphReason::new);
	public static final RegistryObject<MorphReason> MORPHED_BY_ABILITY = MORPH_REASON_REGISTRY.register("morphed_by_ability", MorphReason::new);
	public static final RegistryObject<MorphReason> MORPHED_BY_DELETING_OR_DROPPING_MORPH = MORPH_REASON_REGISTRY.register("morphed_by_deleting_or_dropping_morph", MorphReason::new);
	public static final RegistryObject<MorphReason> MORPHED_BY_COMMAND = MORPH_REASON_REGISTRY.register("morphed_by_command", MorphReason::new);	
	public static final RegistryObject<MorphReason> MORPHED_BY_GAMETEST = MORPH_REASON_REGISTRY.register("morphed_by_gametest", MorphReason::new);	
}
