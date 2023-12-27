package de.budschie.bmorph.tags;

import de.budschie.bmorph.main.References;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class ModAttributeTags
{
	public static TagKey<Attribute> ATTRIBUTES_BLACKLISTED_FOR_COPY = createTag("attributes_blacklisted_for_copy");
	
	public static TagKey<Attribute> createTag(ResourceLocation rl)
	{
		return TagKey.create(Registries.ATTRIBUTE, rl);
	}
	
	public static TagKey<Attribute> createTag(String name)
	{
		return createTag(new ResourceLocation(References.MODID, name));
	}
}
