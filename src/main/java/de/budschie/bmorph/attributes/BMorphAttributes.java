package de.budschie.bmorph.attributes;

import de.budschie.bmorph.main.References;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BMorphAttributes
{
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, References.MODID);
	
    public static final RegistryObject<Attribute> ATTACK_RANGE = ATTRIBUTES.register("attack_range", () -> new RangedAttribute("bmorph.attackRange", 3.5D, 0.0D, 1024.0D).setSyncable(true));
}
