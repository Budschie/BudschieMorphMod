package de.budschie.bmorph.morph.functionality;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.EntityType;

public class AbilityLookupTableHandler
{
	private static HashMap<EntityType<?>, ArrayList<Ability>> abilityLUT = new HashMap<>();
	
	public static synchronized void addAbilityFor(EntityType<?> entityType, Ability ability)
	{
		abilityLUT.computeIfAbsent(entityType, plzIgnoreThis -> new ArrayList<>());
		abilityLUT.get(entityType).add(ability);
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public static ArrayList<Ability> getAbilitiesFor(EntityType<?> entity)
	{
		return abilityLUT.get(entity);
	}
	
	@Nullable
	/** This method will return null if there is no ability for the given entity type. **/
	public static ArrayList<Ability> getAbilitiesFor(MorphItem morphItem)
	{
		return getAbilitiesFor(morphItem.getEntityType());
	}
}
