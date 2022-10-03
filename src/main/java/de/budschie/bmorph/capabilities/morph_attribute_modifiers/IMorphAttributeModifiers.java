package de.budschie.bmorph.capabilities.morph_attribute_modifiers;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * @author budschie
 *	This capability saves the attribute modifiers which the player has received through morphing into a morph.
 *	Using this cap, the mod is able to efficiently revoke the abilities if the player morphs into a different morph.
 */
public interface IMorphAttributeModifiers
{
	public boolean containsSpeedAttribute();
	public void removeAllAttributesFromPlayer(Player player);
	
	public void addAttributeModifier(Attribute attribute, AttributeModifier modifier);
	public void clear();
}
