package de.budschie.bmorph.morph;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.tags.ITag;

public class LazyTag<T> extends LazyRegistryWrapper<ITag<T>> implements Predicate<T>
{
	public LazyTag(ResourceLocation tagName, Function<ResourceLocation, ITag<T>> tagSupplier)
	{
		super(tagName, tagSupplier);
	}

	/**
	 * Tests whether the given instance is contained within the tag. This instance
	 * depends on the generic type that this class has.
	 **/
	@Override
	public boolean test(T element)
	{
		ITag<T> tag = getWrappedType();
		
		return tag == null ?  false : tag.contains(element);
	}
}
