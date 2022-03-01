package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.SerializableUUID;

public class AttributeModifierAbility extends Ability
{
	public static final Codec<AttributeModifierAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.ATTRIBUTE.fieldOf("attribute").forGetter(AttributeModifierAbility::getAttribute),
			ModCodecs.OPERATION.fieldOf("operation").forGetter(inst -> inst.getAttributeModifier().getOperation()),
			Codec.STRING.fieldOf("name").forGetter(inst -> inst.getAttributeModifier().getName()),
			SerializableUUID.CODEC.optionalFieldOf("uuid").forGetter(inst -> Optional.of(inst.getAttributeModifier().getId())),
			Codec.DOUBLE.fieldOf("amount").forGetter(inst -> inst.getAttributeModifier().getAmount())).apply(instance, (attribute, operation, name, uuid, amount) ->
			{
				return new AttributeModifierAbility(attribute, new AttributeModifier(uuid.orElseGet(() -> UUID.randomUUID()), name, amount, operation));
			}));
	
	private AttributeModifier attributeModifier;
	private Attribute attribute;
	
	public AttributeModifierAbility(Attribute attribute, AttributeModifier attributeModifier)
	{
		this.attribute = attribute;
		this.attributeModifier = attributeModifier;
	}
	
	public Attribute getAttribute()
	{
		return attribute;
	}
	
	public AttributeModifier getAttributeModifier()
	{
		return attributeModifier;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		if(!player.level.isClientSide)
			player.getAttribute(attribute).addTransientModifier(attributeModifier);
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		if(!player.level.isClientSide)
			player.getAttribute(attribute).removeModifier(attributeModifier);
	}
}
