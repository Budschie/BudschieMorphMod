package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.json_integration.NBTPath;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.EntityUtil;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TransformEntityOnDeath extends AbstractEventAbility
{	
	public static Codec<TransformEntityOnDeath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.ENTITIES.fieldOf("transform_from").forGetter(TransformEntityOnDeath::getEntityToTransformFrom),
			ModCodecs.ENTITIES.fieldOf("transform_to").forGetter(TransformEntityOnDeath::getEntityToTransformTo),
			NbtMappings.CODEC.listOf().optionalFieldOf("nbt_mappings", Arrays.asList()).forGetter(TransformEntityOnDeath::getNbtMappings),
			Codec.STRING.listOf().optionalFieldOf("permitted_deaths").forGetter(TransformEntityOnDeath::getPermittedDeaths),
			SoundInstance.CODEC.optionalFieldOf("transformation_sound").forGetter(TransformEntityOnDeath::getSoundToPlay))
			.apply(instance, TransformEntityOnDeath::new));
	
	private EntityType<?> entityToTransformFrom;
	private EntityType<?> entityToTransformTo;
	private List<NbtMappings> nbtMappings;
	private Optional<List<String>> permittedDeaths;
	private Optional<SoundInstance> soundToPlay;
	
	public TransformEntityOnDeath(EntityType<?> entityToTransformFrom, EntityType<?> entityToTransformTo, List<NbtMappings> nbtMappings, Optional<List<String>> permittedDeaths,
			Optional<SoundInstance> soundToPlay)
	{
		this.entityToTransformFrom = entityToTransformFrom;
		this.entityToTransformTo = entityToTransformTo;
		this.nbtMappings = nbtMappings;
		this.permittedDeaths = permittedDeaths;
		this.soundToPlay = soundToPlay;
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
			
			// Read the additional save data for the new entity
			EntityUtil.readAdditionalSaveData(newEntity, tagNew);
			
			// Add new entity
			event.getEntity().level.addFreshEntity(newEntity);
			
			// Remove old entity
			event.getEntity().discard();
			
			// Play sound
			soundToPlay.ifPresent(sound -> sound.playSoundAt(newEntity));
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

	public Optional<List<String>> getPermittedDeaths()
	{
		return permittedDeaths;
	}

	public Optional<SoundInstance> getSoundToPlay()
	{
		return soundToPlay;
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
