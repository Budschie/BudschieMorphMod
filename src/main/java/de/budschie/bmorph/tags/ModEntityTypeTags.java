package de.budschie.bmorph.tags;

import de.budschie.bmorph.main.References;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModEntityTypeTags
{
	// Horrible name, but I couldn't think of any better name
	public static TagKey<EntityType<?>> DISABLE_SNEAK_TRANSFORM = createTag("disable_sneak_transform");
	// Tag an entity with this type if it extends from the iron golem class
	public static TagKey<EntityType<?>> IRON_GOLEM_ALIKE = createTag("iron_golem_alike");
	// Tag for undead mobs
	public static TagKey<EntityType<?>> UNDEAD = createTag("undead");
	
	public static TagKey<EntityType<?>> createTag(ResourceLocation rl)
	{
		return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, rl);
	}
	
	public static TagKey<EntityType<?>> createTag(String name)
	{
		return createTag(new ResourceLocation(References.MODID, name));
	}
}
