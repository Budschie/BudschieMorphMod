package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class GhostAbility extends Ability
{
    public static final Codec<GhostAbility> CODEC = Codec.unit(GhostAbility::new);

    @Override
    public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
    {
        super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);

        MorphUtil.getCapOrNull(player).setGhost(true);
    }

    @Override
    public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
    {
        super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);

        MorphUtil.getCapOrNull(player).setGhost(false);
    }
}
