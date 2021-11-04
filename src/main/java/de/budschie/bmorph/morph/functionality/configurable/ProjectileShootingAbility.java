package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public class ProjectileShootingAbility extends StunAbility
{
	public static Codec<ProjectileShootingAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					ModCodecs.ENTITIES.fieldOf("projectile_entity").forGetter(ProjectileShootingAbility::getProjectileEntityType),
					Codec.INT.optionalFieldOf("stun", 40).forGetter(ProjectileShootingAbility::getStun),
					Codec.DOUBLE.optionalFieldOf("motion", 0.0D).forGetter(ProjectileShootingAbility::getMotion),
					Codec.DOUBLE.optionalFieldOf("acceleration", 0.0D).forGetter(ProjectileShootingAbility::getAcceleration),
					CompoundNBT.CODEC.optionalFieldOf("nbt", new CompoundNBT()).forGetter(ProjectileShootingAbility::getNbtData),
					SoundInstance.CODEC.optionalFieldOf("sound").forGetter(ProjectileShootingAbility::getSoundInstance)
					).apply(instance, ProjectileShootingAbility::new));
	
	private EntityType<?> projectileEntityType;
	private double motion;
	private double acceleration;
	private CompoundNBT nbtData;
	private Optional<SoundInstance> soundInstance;
	
	public ProjectileShootingAbility(EntityType<?> projectileEntityType, int stun, double motion, double acceleration, CompoundNBT nbtData, Optional<SoundInstance> soundInstance)
	{
		super(stun);
		this.motion = motion;
		this.projectileEntityType = projectileEntityType;
		this.acceleration = acceleration;
		this.nbtData = nbtData;
		this.soundInstance = soundInstance;
	}
	
	public EntityType<?> getProjectileEntityType()
	{
		return projectileEntityType;
	}
	
	public double getMotion()
	{
		return motion;
	}
	
	public double getAcceleration()
	{
		return acceleration;
	}
	
	public CompoundNBT getNbtData()
	{
		return nbtData;
	}
	
	public Optional<SoundInstance> getSoundInstance()
	{
		return soundInstance;
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
//			Entity createdEntity = projectileSupplier.apply(player, Vector3d.fromPitchYaw(player.getPitchYaw()));
			
			Entity createdEntity = projectileEntityType.create(player.getEntityWorld());
			
			createdEntity.read(nbtData);
			
			Vector3d dir = Vector3d.fromPitchYaw(player.getPitchYaw());
			
			createdEntity.setMotion(dir.x * motion, dir.y * motion, dir.z * motion);
			
			createdEntity.setPosition(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
			
			if(createdEntity instanceof DamagingProjectileEntity)
			{
				DamagingProjectileEntity dmgProjectile = (DamagingProjectileEntity) createdEntity;
				
				dmgProjectile.accelerationX = dir.x * acceleration;
				dmgProjectile.accelerationY = dir.y * acceleration;
				dmgProjectile.accelerationZ = dir.z * acceleration;
			}
			
			player.world.addEntity(createdEntity);
			stun(player.getUniqueID());
			
			if(this.soundInstance.isPresent())
				this.soundInstance.get().playSoundAt(player);
		}		
	}
}
