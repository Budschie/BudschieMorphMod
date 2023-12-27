package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class SecondaryAbility extends Ability
{
	public static final Codec<SecondaryAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.ABILITY.fieldOf("secondary_ability").forGetter(SecondaryAbility::getSecondaryAbility)).apply(instance, SecondaryAbility::new));
	
	private LazyOptional<Ability> secondaryAbility;
	
	public SecondaryAbility(LazyOptional<Ability> secondaryAbility)
	{
		this.secondaryAbility = secondaryAbility;
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(player.isCrouching())
		{
			secondaryAbility.resolve().get().onUsedAbility(player, currentMorph);
		}
	}
	
	public LazyOptional<Ability> getSecondaryAbility()
	{
		return secondaryAbility;
	}
}
