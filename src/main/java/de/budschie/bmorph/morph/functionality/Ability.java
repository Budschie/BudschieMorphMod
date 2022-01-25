package de.budschie.bmorph.morph.functionality;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.configurable.ConfigurableAbility;
import de.budschie.bmorph.util.IDynamicRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public abstract class Ability implements IDynamicRegistryObject
{
	private final Logger LOGGER = LogManager.getLogger();
	
	private ResourceLocation resourceLocation;
	private ConfigurableAbility<? extends Ability> configurableAbility;
	
	public void enableAbility(Player player, MorphItem enabledItem) {}
	
	public void disableAbility(Player player, MorphItem disabledItem) {}
	
	/** This method is fired when an active ability is used. **/
	public void onUsedAbility(Player player, MorphItem currentMorph) {}
	
	public void onRegister() {}
	
	public void onUnregister() {}
	
	@Override
	public ResourceLocation getResourceLocation()
	{
		return resourceLocation;
	}
	
	@Override
	public void setResourceLocation(ResourceLocation resourceLocation)
	{
		this.resourceLocation = resourceLocation;
	}
	
	public ConfigurableAbility<? extends Ability> getConfigurableAbility()
	{
		return configurableAbility;
	}
	
	public void setConfigurableAbility(ConfigurableAbility<?> configurableAbility)
	{
		this.configurableAbility = configurableAbility;
	}
	
	@Override
	public int hashCode()
	{
		return resourceLocation.hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Ability otherAbility)
		{
			if(otherAbility.getResourceLocation().equals(this.getResourceLocation()))
				return true;
		}
		
		return false;
	}
}
