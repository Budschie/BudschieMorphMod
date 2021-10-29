package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.entity.player.PlayerEntity;

public class InstaJumpAbility extends Ability
{
	public static final Codec<InstaJumpAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.FLOAT.fieldOf("stepHeight").forGetter(InstaJumpAbility::getStepHeight)).apply(instance, InstaJumpAbility::new));
	
	private float stepHeight;
	
	public InstaJumpAbility(float stepHeight)
	{
		this.stepHeight = stepHeight;
	}
	
	public float getStepHeight()
	{
		return stepHeight;
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
//		player.stepHeight = 1.45f;
		player.stepHeight = this.stepHeight;
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.stepHeight = 0.6f;
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
