package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class Ability implements IForgeRegistryEntry<Ability>
{
	private ResourceLocation registryName;
	
	public abstract void enableAbility(PlayerEntity player, MorphItem enabledItem);
	
	public abstract void disableAbility(PlayerEntity player, MorphItem disabledItem);
	
	/** This method is fired when an active ability is used. **/
	public abstract void onUsedAbility(PlayerEntity player, MorphItem currentMorph);
	
	@Override
	public Ability setRegistryName(ResourceLocation name)
	{
		registryName = name;
		return this;
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return registryName;
	}
	
	@Override
	public Class<Ability> getRegistryType()
	{
		return Ability.class;
	}
}
