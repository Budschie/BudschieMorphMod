package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.player.PlayerEntity;

public class MobAttackAbility extends Ability
{
	public static final Codec<MobAttackAbility> CODEC = ModCodecs.newCodec(() -> new MobAttackAbility());
	
	/** An entity predicate defining entities by which you can be attacked would be nice in the future. But we live in the sad present, where we don't have any CODEC for EntityPredicate's, so we have to
	 * put that feature onto our todo list. 
	**/
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		MorphUtil.processCap(player, cap -> cap.setMobAttack(true));
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		MorphUtil.processCap(player, cap -> cap.setMobAttack(false));
	}
}
