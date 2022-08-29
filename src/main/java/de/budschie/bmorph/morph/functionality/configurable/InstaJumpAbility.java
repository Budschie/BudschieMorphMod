package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

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
	
	// TODO: Port from maxUpStep to attribute based system
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
//		player.stepHeight = 1.45f;
		player.maxUpStep = this.stepHeight;
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		player.maxUpStep = 0.6f;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		
	}
}
