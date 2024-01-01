package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EffectWithinRadiusAbility extends Ability
{
    public static final Codec<EffectWithinRadiusAbility> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ModCodecs.EFFECT_INSTANCE.listOf().fieldOf("effects").forGetter(EffectWithinRadiusAbility::getEffects),
                    Codec.INT.fieldOf("radius").forGetter(EffectWithinRadiusAbility::getRadius),
                            Codec.BOOL.fieldOf("affects_self").forGetter(EffectWithinRadiusAbility::shouldAffectSelf))
                    .apply(instance, EffectWithinRadiusAbility::new));

    private final List<MobEffectInstance> effects;
    private final int radius;
    private final boolean affectsSelf;

    public EffectWithinRadiusAbility(List<MobEffectInstance> effects, int radius, boolean affectsSelf)
    {
        this.effects = effects;
        this.radius = radius;
        this.affectsSelf = affectsSelf;
    }

    @Override
    public void onUsedAbility(Player player, MorphItem currentMorph)
    {
        Entity toIgnore = null;

        if(!affectsSelf)
        {
            toIgnore = player;
        }

        List<Entity> entities = player.getLevel().getEntities(null, new AABB(player.position().subtract(new Vec3(radius, radius, radius)),
                player.position().add(new Vec3(radius, radius, radius))));

        for(Entity entity : entities)
        {
            if(!(entity instanceof LivingEntity living))
            {
                return;
            }

            for(MobEffectInstance effect : effects)
            {
                living.addEffect(new MobEffectInstance(effect));
            }
        }
    }

    public int getRadius()
    {
        return radius;
    }

    public List<MobEffectInstance> getEffects()
    {
        return effects;
    }

    public boolean shouldAffectSelf()
    {
        return affectsSelf;
    }
}
