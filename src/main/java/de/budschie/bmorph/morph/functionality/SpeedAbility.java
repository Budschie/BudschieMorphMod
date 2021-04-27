package de.budschie.bmorph.morph.functionality;

import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;

public class SpeedAbility extends Ability
{
	private static final UUID NORMAL_MOVEMENT_UUID = new UUID(8788, 1097);
	private float strength;
	
	public SpeedAbility(float strength)
	{
		this.strength = strength;
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		if(player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(NORMAL_MOVEMENT_UUID) == null)
			player.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(new AttributeModifier(NORMAL_MOVEMENT_UUID, "mvmntLand", strength, Operation.MULTIPLY_BASE));
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(NORMAL_MOVEMENT_UUID);
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
