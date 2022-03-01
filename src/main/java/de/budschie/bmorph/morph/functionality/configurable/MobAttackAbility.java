package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;

public class MobAttackAbility extends Ability
{
	public static final Codec<MobAttackAbility> CODEC = Codec.unit(() -> new MobAttackAbility());
	
	/** An entity predicate defining entities by which you can be attacked would be nice in the future. But we live in the sad present, where we don't have any CODEC for EntityPredicate's, so we have to
	 * put that feature onto our todo list. 
	**/
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		MorphUtil.processCap(player, cap -> cap.setMobAttack(true));
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		MorphUtil.processCap(player, cap -> cap.setMobAttack(false));
	}
}
