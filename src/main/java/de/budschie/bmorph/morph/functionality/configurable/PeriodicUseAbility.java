package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.*;

public class PeriodicUseAbility extends Ability
{
    public static final Codec<PeriodicUseAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ModCodecs.ABILITY_LIST.fieldOf("abilities_to_use").forGetter(PeriodicUseAbility::getAbilitiesToUse),
            Codec.INT.fieldOf("period_length").forGetter(PeriodicUseAbility::getPeriodLength),
            Codec.INT.optionalFieldOf("initial_lag").forGetter(PeriodicUseAbility::getInitialLag),
            Codec.BOOL.fieldOf("preserve_time_between_morphs").forGetter(PeriodicUseAbility::shouldPreserveTimeBetweenMorphs)
    ).apply(instance, PeriodicUseAbility::new));

    private final LazyOptional<List<Ability>> abilitiesToUse;
    private final int periodLength;
    private final Optional<Integer> initialLag;
    private final boolean shouldPreserveTimeBetweenMorphs;

    private final HashMap<UUID, TickTimestamp> playerTimestamps = new HashMap<>();

    public PeriodicUseAbility(LazyOptional<List<Ability>> abilitiesToUse, int periodLength, Optional<Integer> initialLag, boolean shouldPreserveTimeBetweenMorphs)
    {
        this.abilitiesToUse = abilitiesToUse;
        this.periodLength = periodLength;
        this.initialLag = initialLag;
        this.shouldPreserveTimeBetweenMorphs = shouldPreserveTimeBetweenMorphs;
    }

    public LazyOptional<List<Ability>> getAbilitiesToUse()
    {
        return abilitiesToUse;
    }

    public int getPeriodLength()
    {
        return periodLength;
    }

    public Optional<Integer> getInitialLag()
    {
        return initialLag;
    }

    public boolean shouldPreserveTimeBetweenMorphs()
    {
        return shouldPreserveTimeBetweenMorphs;
    }

    @Override
    public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
    {
        super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);

        playerTimestamps.put(player.getUUID(), new TickTimestamp(initialLag.orElse(0)));
    }

    @Override
    public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
    {
        super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);

        playerTimestamps.remove(player.getUUID());
    }

    @Override
    public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
    {
        super.serialize(player, context, canSaveTransientData);
        
        if(!canSaveTransientData && !shouldPreserveTimeBetweenMorphs)
        {
            return;
        }

        CompoundTag tag = null;

        AbilitySerializationContext.AbilitySerializationObject serializationObject = context.getOrCreateSerializationObjectForAbility(this);

        if(shouldPreserveTimeBetweenMorphs)
        {
            tag = serializationObject.createPersistentTag();
        }
        else
        {
            tag = serializationObject.createTransientTag();
        }

        tag.putInt("negative_time_to_elapse", playerTimestamps.get(player.getUUID()).getTimeElapsed());
    }

    @Override
    public void deserialize(Player player, AbilitySerializationContext context)
    {
        super.deserialize(player, context);

        AbilitySerializationContext.AbilitySerializationObject serializationObject = context.getOrCreateSerializationObjectForAbility(this);

        Optional<CompoundTag> tag = Optional.empty();

        if(shouldPreserveTimeBetweenMorphs)
        {
            tag = serializationObject.getPersistentTag();
        }
        else
        {
            tag = serializationObject.getTransientTag();
        }

        if(tag.isEmpty())
        {
            return;
        }

        playerTimestamps.put(player.getUUID(), new TickTimestamp(tag.get().getInt("negative_time_to_elapse")));
    }

    private void useAbilities(Player player)
    {
        for(Ability ability : this.abilitiesToUse.resolve().get())
        {
            IMorphCapability cap = MorphUtil.getCapOrNull(player);
            ability.onUsedAbility(player, cap.getCurrentMorph().orElse(null));
        }
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.ServerTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
        {
            return;
        }

        if(event.side != LogicalSide.SERVER)
        {
            return;
        }

        List<UUID> resets = new ArrayList<>();

        for(Map.Entry<UUID, TickTimestamp> entry : playerTimestamps.entrySet())
        {
            if(entry.getValue().getTimeElapsed() >= 0)
            {
                resets.add(entry.getKey());
                useAbilities(event.getServer().getPlayerList().getPlayer(entry.getKey()));
            }
        }

        for(UUID uuid : resets)
        {
            playerTimestamps.put(uuid, new TickTimestamp(-periodLength));
        }
    }

    @Override
    public boolean isAbleToReceiveEvents()
    {
        return true;
    }
}
