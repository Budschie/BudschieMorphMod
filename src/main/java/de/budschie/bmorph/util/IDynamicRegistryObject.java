package de.budschie.bmorph.util;

import net.minecraft.resources.ResourceLocation;

public interface IDynamicRegistryObject
{
	/** Returns the name of this dynamic registry object. **/
	ResourceLocation getResourceLocation();
	
	/** Sets the name of this dynamic registry object. **/
	void setResourceLocation(ResourceLocation name);
}
