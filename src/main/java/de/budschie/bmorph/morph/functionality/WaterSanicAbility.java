package de.budschie.bmorph.morph.functionality;

import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.ForgeMod;

public class WaterSanicAbility extends Ability
{
	private static final UUID WATER_MOVEMENT_UUID = new UUID(897, 22455);
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		if(player.getAttribute(ForgeMod.SWIM_SPEED.get()).getModifier(WATER_MOVEMENT_UUID) == null)
			player.getAttribute(ForgeMod.SWIM_SPEED.get()).applyNonPersistentModifier(new AttributeModifier(WATER_MOVEMENT_UUID, "mvmntWater", 2.25f, Operation.MULTIPLY_BASE));
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.getAttribute(ForgeMod.SWIM_SPEED.get()).removeModifier(WATER_MOVEMENT_UUID);
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
	}

}
