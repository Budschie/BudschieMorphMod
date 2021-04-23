package de.budschie.bmorph.morph.functionality;

import java.util.function.Supplier;

import de.budschie.bmorph.main.References;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = Bus.MOD)
public class AbilityRegistry
{	
	public static DeferredRegister<Ability> ABILITY_REGISTRY = DeferredRegister.create(Ability.class, References.MODID); 
	public static Supplier<IForgeRegistry<Ability>> REGISTRY = ABILITY_REGISTRY.makeRegistry("abilities", () -> new RegistryBuilder<>());
	
	public static RegistryObject<Ability> FLY_ABILITY = ABILITY_REGISTRY.register("flying", () -> new FlyAbility());
}
