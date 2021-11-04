package de.budschie.bmorph.morph;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;

/**
 * The aim of this class is to provide a way of easily loading and later
 * resolving INamedTags.
 **/
public class LazyTag<T> implements Predicate<T>
{
	private ResourceLocation tagName;
	private Function<ResourceLocation, ITag<T>> tagSupplier;
	private boolean hasTag = true;
	private ITag<T> cachedTag;
	
	/**
	 * This is the constructor of the lazy tag.
	 * 
	 * @param tagName     This is the tag name that this lazy tag has. It will be
	 *                    used to retrieve a named tag instance later.
	 * @param tagSupplier This is a function taking in a ResourceLocation and
	 *                    returning a named tag. This function shall provide this
	 *                    lazy tag instance with a way of resolving resource
	 *                    locations to tag names.
	 **/
	public LazyTag(ResourceLocation tagName, Function<ResourceLocation, ITag<T>> tagSupplier)
	{
		this.tagName = tagName;
		this.tagSupplier = tagSupplier;
	}
	
	public ResourceLocation getTagName()
	{
		return tagName;
	}
	
	/**
	 * Tests whether the given instance is contained within the tag. This instance
	 * depends on the generic type that this class has.
	 **/
	@Override
	public boolean test(T t)
	{
		if(hasTag && cachedTag == null)
		{
			this.cachedTag = tagSupplier.apply(tagName);
			
			if(this.cachedTag == null)
				this.hasTag = false;
		}
		
		return hasTag ? this.cachedTag.contains(t) : false;
	}
}
