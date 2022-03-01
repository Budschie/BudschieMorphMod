package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.json_integration.NBTPath;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import de.budschie.bmorph.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TransformEntityOnDeath extends Ability
{	
	public static Codec<TransformEntityOnDeath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.ENTITIES.fieldOf("transform_from").forGetter(TransformEntityOnDeath::getEntityToTransformFrom),
			ModCodecs.ENTITIES.fieldOf("transform_to").forGetter(TransformEntityOnDeath::getEntityToTransformTo),
			NbtMappings.CODEC.listOf().optionalFieldOf("nbt_mappings", Arrays.asList()).forGetter(TransformEntityOnDeath::getNbtMappings),
			ModCodecs.DATA_TRANSFORMER.listOf().optionalFieldOf("data_transformers", Arrays.asList()).forGetter(TransformEntityOnDeath::getDataTransformers),
			Codec.STRING.listOf().optionalFieldOf("permitted_deaths").forGetter(TransformEntityOnDeath::getPermittedDeaths),
			AudioVisualEffect.CODEC.optionalFieldOf("transformed_entity_effect").forGetter(TransformEntityOnDeath::getTransformedEntityEffect),
			AudioVisualEffect.CODEC.optionalFieldOf("transformer_entity_effect").forGetter(TransformEntityOnDeath::getTransformerEntityEffect))
			.apply(instance, TransformEntityOnDeath::new));
	
	private EntityType<?> entityToTransformFrom;
	private EntityType<?> entityToTransformTo;
	private List<NbtMappings> nbtMappings;
	private List<LazyOptional<DataTransformer>> dataTransformers;
	private Optional<List<String>> permittedDeaths;
	private Optional<AudioVisualEffect> transformedAudioVisualEffect;
	private Optional<AudioVisualEffect> transformerAudioVisualEffect;
	
	public TransformEntityOnDeath(EntityType<?> entityToTransformFrom, EntityType<?> entityToTransformTo, List<NbtMappings> nbtMappings, List<LazyOptional<DataTransformer>> dataTransformers, Optional<List<String>> permittedDeaths,
			Optional<AudioVisualEffect> transformedAudioVisualEffect, Optional<AudioVisualEffect> transformerAudioVisualEffect)
	{
		this.entityToTransformFrom = entityToTransformFrom;
		this.entityToTransformTo = entityToTransformTo;
		this.nbtMappings = nbtMappings;
		this.dataTransformers = dataTransformers;
		this.permittedDeaths = permittedDeaths;
		this.transformedAudioVisualEffect = transformedAudioVisualEffect;
		this.transformerAudioVisualEffect = transformerAudioVisualEffect;
	}

	@SubscribeEvent
	public void onLivingEntityKilled(LivingDeathEvent event)
	{
		// Check if the entity can be transformed by this ability and if the damager is a player with this ability and if the damage source is valid 
		if(event.getEntity().getType() == entityToTransformFrom && event.getSource() != null && event.getSource().getEntity() != null && isTracked(event.getSource().getEntity()) && (permittedDeaths.isEmpty() || permittedDeaths.get().contains(event.getSource().getMsgId())))
		{
			// If these checks pass, remove the entity from the world **immediately** and replace it by another entity.
			Entity newEntity = entityToTransformTo.create(event.getEntity().level);
			
			// Synchronize rotation and position for a seamless transition.
			EntityUtil.copyLocationAndRotation(event.getEntity(), newEntity);
			
//			newEntity.setDeltaMovement(event.getEntity().getDeltaMovement());
			
			CompoundTag tagOld = new CompoundTag();
			CompoundTag tagNew = new CompoundTag();
			
			// Create compound tag from the entity that was killed
			EntityUtil.addAdditionalSaveData(event.getEntity(), tagOld);
			
			// Copy all important values over from the old entity to the new entity
			nbtMappings.forEach(mapping -> mapping.copy(tagOld, tagNew));
			
			// Apply data transformer
			dataTransformers.forEach(dataTransformer -> dataTransformer.resolve().get().transformData(tagOld, tagNew));
			
			// Read the additional save data for the new entity
			EntityUtil.readAdditionalSaveData(newEntity, tagNew);
			
			// Add new entity
			event.getEntity().level.addFreshEntity(newEntity);
			
			// Remove old entity
			event.getEntity().discard();
			
			// Play sound
			if(!event.getEntity().level.isClientSide())
			{
				this.transformedAudioVisualEffect.ifPresent(effect -> effect.playEffect(newEntity));
				this.transformerAudioVisualEffect.ifPresent(effect -> effect.playEffect(event.getSource().getEntity()));
			}
		}
	}
	
	public EntityType<?> getEntityToTransformFrom()
	{
		return entityToTransformFrom;
	}

	public EntityType<?> getEntityToTransformTo()
	{
		return entityToTransformTo;
	}

	public List<NbtMappings> getNbtMappings()
	{
		return nbtMappings;
	}
	
	public List<LazyOptional<DataTransformer>> getDataTransformers()
	{
		return dataTransformers;
	}

	public Optional<List<String>> getPermittedDeaths()
	{
		return permittedDeaths;
	}

	public Optional<AudioVisualEffect> getTransformedEntityEffect()
	{
		return transformedAudioVisualEffect;
	}
	
	public Optional<AudioVisualEffect> getTransformerEntityEffect()
	{
		return transformerAudioVisualEffect;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}

	public static class NbtMappings
	{
		public static final Codec<NbtMappings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				NBTPath.CODEC.fieldOf("copy_nbt_source").forGetter(NbtMappings::getSourcePath), 
				NBTPath.CODEC.fieldOf("copy_nbt_dest").forGetter(NbtMappings::getDestinationPath))
				.apply(instance, NbtMappings::new));
		
		private NBTPath src;
		private NBTPath dest;
		
		public NbtMappings(NBTPath src, NBTPath dest)
		{
			this.src = src;
			this.dest = dest;
		}
		
		public NBTPath getSourcePath()
		{
			return src;
		}
		
		public NBTPath getDestinationPath()
		{
			return dest;
		}
		
		public void copy(CompoundTag from, CompoundTag to)
		{
			// Copy the given data from the src to the destination
			src.copyTo(from, to, dest);
		}
	}
}
