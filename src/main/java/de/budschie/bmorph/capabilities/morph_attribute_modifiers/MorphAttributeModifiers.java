package de.budschie.bmorph.capabilities.morph_attribute_modifiers;

import com.google.common.collect.HashMultimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class MorphAttributeModifiers implements IMorphAttributeModifiers
{
	private HashMultimap<Attribute, AttributeModifier> attributeMap = HashMultimap.create();
	
	@Override
	public void addAttributeModifier(Attribute attribute, AttributeModifier modifier)
	{
		attributeMap.put(attribute, modifier);
	}

	@Override
	public void clear()
	{
		attributeMap.clear();
	}

	@Override
	public void removeAllAttributesFromPlayer(Player player)
	{
		player.getAttributes().removeAttributeModifiers(attributeMap);
	}

	@Override
	public boolean containsSpeedAttribute()
	{
		return attributeMap.containsKey(Attributes.MOVEMENT_SPEED);
	}
}
