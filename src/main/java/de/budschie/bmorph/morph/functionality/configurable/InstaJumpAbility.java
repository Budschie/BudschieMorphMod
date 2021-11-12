package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;

public class InstaJumpAbility extends Ability
{
	public static final Codec<InstaJumpAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.FLOAT.fieldOf("step_height").forGetter(InstaJumpAbility::getStepHeight)).apply(instance, InstaJumpAbility::new));
	
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
	public void enableAbility(Player player, MorphItem enabledItem)
	{
//		player.stepHeight = 1.45f;
		player.maxUpStep = this.stepHeight;
	}

	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		player.maxUpStep = 0.6f;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		
	}
}
