package de.budschie.bmorph.morph.functionality;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.configurable.ConfigurableAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public abstract class Ability
{
	private final Logger LOGGER = LogManager.getLogger();
	
	private ResourceLocation resourceLocation;
	private ConfigurableAbility<? extends Ability> configurableAbility;
	
	public void enableAbility(PlayerEntity player, MorphItem enabledItem) {}
	
	public void disableAbility(PlayerEntity player, MorphItem disabledItem) {}
	
	/** This method is fired when an active ability is used. **/
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph) {}
	
	public void onRegister()
	{
		
	}
	
	public void onUnregister()
	{
		
	}
	
	public ResourceLocation getResourceLocation()
	{
		return resourceLocation;
	}
	
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
		if(obj instanceof Ability)
		{
			Ability otherAbility = (Ability) obj;
			
			if(otherAbility.getResourceLocation().equals(this.getResourceLocation()))
				return true;
		}
		
		return false;
	}
}
