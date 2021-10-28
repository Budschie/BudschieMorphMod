package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.UUIDCodec;

public class AttributeModifierAbility extends Ability
{
	public static Codec<AttributeModifierAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.ATTRIBUTE.fieldOf("attribute").forGetter(AttributeModifierAbility::getAttribute),
			ModCodecs.OPERATION.fieldOf("operation").forGetter(inst -> inst.getAttributeModifier().getOperation()),
			Codec.STRING.fieldOf("name").forGetter(inst -> inst.getAttributeModifier().getName()),
			UUIDCodec.CODEC.optionalFieldOf("uuid").forGetter(inst -> Optional.of(inst.getAttributeModifier().getID())),
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
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		player.getAttribute(attribute).applyNonPersistentModifier(attributeModifier);
	}
	
	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.getAttribute(attribute).removeModifier(attributeModifier);
	}
}
