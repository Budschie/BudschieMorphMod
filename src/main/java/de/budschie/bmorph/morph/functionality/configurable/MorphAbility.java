package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.fallback.FallbackMorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.morph.functionality.configurable.TransformEntityOnDeath.NbtMappings;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class MorphAbility extends Ability
{
	public static enum MorphMode
	{
		ADD_TO_LIST, MORPH, BOTH
	}
	
	public static final Codec<MorphMode> MORPH_MODE_CODEC = ModCodecs.getEnumCodec(MorphMode.class, MorphMode::values);
	
	public static final Codec<MorphAbility> CODEC = RecordCodecBuilder.create(instance ->
	{
		return instance
				.group(MORPH_MODE_CODEC.fieldOf("morph_mode").forGetter(MorphAbility::getMorphMode),
						ModCodecs.ENTITIES.fieldOf("entity_to_morph_to").forGetter(MorphAbility::getEntityToMorphTo),
						NbtMappings.CODEC.listOf().optionalFieldOf("nbt_mappings", Arrays.asList()).forGetter(MorphAbility::getNbtMappings),
						ModCodecs.DATA_TRANSFORMER.listOf().optionalFieldOf("data_transformers", Arrays.asList()).forGetter(MorphAbility::getDataTransformers),
						AudioVisualEffect.CODEC.optionalFieldOf("effect_on_morph").forGetter(MorphAbility::getEffectOnMorph),
						ModCodecs.NBT_COMPOUND_TAG.optionalFieldOf("default_nbt", new CompoundTag()).forGetter(MorphAbility::getDefaultNbt))
				.apply(instance, MorphAbility::new);
	});
	
	public MorphAbility(MorphMode morphMode, EntityType<?> entityToMorphTo, List<NbtMappings> nbtMappings, List<LazyOptional<DataTransformer>> dataTransformers,
			Optional<AudioVisualEffect> effectOnMorph, CompoundTag defaultNbt)
	{
		this.morphMode = morphMode;
		this.entityToMorphTo = entityToMorphTo;
		this.nbtMappings = nbtMappings;
		this.dataTransformers = dataTransformers;
		this.effectOnMorph = effectOnMorph;
		this.defaultNbt = defaultNbt;
	}

	private MorphMode morphMode;
	
	private EntityType<?> entityToMorphTo;
	
	private List<NbtMappings> nbtMappings;
	private List<LazyOptional<DataTransformer>> dataTransformers;
	private Optional<AudioVisualEffect> effectOnMorph;
	
	private CompoundTag defaultNbt;
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		super.onUsedAbility(player, currentMorph);
		
		CompoundTag newTag = defaultNbt.copy();
		
		if(currentMorph instanceof FallbackMorphItem fallbackItem)
		{
			CompoundTag oldNbt = fallbackItem.serializeAdditional();
			
			nbtMappings.forEach(mapping -> mapping.copy(oldNbt, newTag));
			dataTransformers.forEach(dataTransformer -> dataTransformer.resolve().get().transformData(oldNbt, newTag));
		}
		
		FallbackMorphItem item = new FallbackMorphItem(newTag, entityToMorphTo);
		
		if(morphMode == MorphMode.ADD_TO_LIST || morphMode == MorphMode.BOTH)
		{
			IMorphCapability morphCap = MorphUtil.getCapOrNull(player);
			
			if(!morphCap.getMorphList().contains(item))
			{
				// Add to morph list and sync it with all clients (I should really change that "all clients" stuff netcode bullsh*t in the future tho)
				morphCap.syncMorphAcquisition(item);
			}
		}
		
		if(morphMode == MorphMode.MORPH || morphMode == MorphMode.BOTH)
		{
			MorphUtil.morphToServer(Optional.of(item), MorphReasonRegistry.MORPHED_BY_ABILITY.get(), player);
		}
		
		effectOnMorph.ifPresent(effect -> effect.playEffect(player));
	}
	
	public MorphMode getMorphMode()
	{
		return morphMode;
	}
	
	public EntityType<?> getEntityToMorphTo()
	{
		return entityToMorphTo;
	}
	
	public List<NbtMappings> getNbtMappings()
	{
		return nbtMappings;
	}
	
	public List<LazyOptional<DataTransformer>> getDataTransformers()
	{
		return dataTransformers;
	}
	
	public Optional<AudioVisualEffect> getEffectOnMorph()
	{
		return effectOnMorph;
	}
	
	public CompoundTag getDefaultNbt()
	{
		return defaultNbt;
	}
}
