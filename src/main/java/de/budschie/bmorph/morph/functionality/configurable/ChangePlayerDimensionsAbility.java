package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public class ChangePlayerDimensionsAbility extends Ability
{
    public static final Codec<ChangePlayerDimensionsAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group
    (
            Codec.FLOAT.fieldOf("width").forGetter(ChangePlayerDimensionsAbility::getWidth),
            Codec.FLOAT.fieldOf("height").forGetter(ChangePlayerDimensionsAbility::getHeight)
    ).apply(instance, ChangePlayerDimensionsAbility::new));

    private float width;
    private float height;

    public ChangePlayerDimensionsAbility(float width, float height)
    {
        this.width = width;
        this.height = height;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    @Override
    public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
    {
        super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);

        MorphUtil.getCapOrNull(player).setOverrideEntityDimensions(Optional.of(EntityDimensions.scalable(this.width, this.height)));
    }

    @Override
    public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
    {
        super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);

        MorphUtil.getCapOrNull(player).setOverrideEntityDimensions(Optional.empty());
    }
}
