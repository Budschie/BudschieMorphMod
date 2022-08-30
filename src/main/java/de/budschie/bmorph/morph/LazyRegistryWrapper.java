package de.budschie.bmorph.morph;

import java.text.MessageFormat;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;

/**
 * The aim of this class is to provide a way of easily loading and later
 * resolving any kind of dynamic registry object.
 **/
public class LazyRegistryWrapper<T>
{	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private ResourceLocation tagName;
	private Function<ResourceLocation, T> tagSupplier;
	private boolean hasTag = true;
	private boolean hasErrored = false;
	private T cachedTag;
	
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
	public LazyRegistryWrapper(ResourceLocation tagName, Function<ResourceLocation, T> tagSupplier)
	{
		this.tagName = tagName;
		this.tagSupplier = tagSupplier;
	}
	
	public ResourceLocation getResourceLocation()
	{
		return tagName;
	}
		
	private void resolveIfNotPresent()
	{
		if(hasTag && cachedTag == null)
		{
			this.cachedTag = tagSupplier.apply(tagName);
			
			if(this.cachedTag == null)
			{
				this.hasTag = false;
				
				if(!hasErrored)
				{
					LOGGER.warn(MessageFormat.format("The resource location {0} could not be resolved to a registry type.", tagName.toString()));
					hasErrored = true;
				}
			}
		}
	}
	
	/** Resolves the tag and returns null if that failed. **/
	public T getWrappedType()
	{
		resolveIfNotPresent();
		return cachedTag;
	}
}
