package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class ProjectileShootingAbility extends StunAbility
{
	public static Codec<ProjectileShootingAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					ModCodecs.ENTITIES.fieldOf("projectile_entity").forGetter(ProjectileShootingAbility::getProjectileEntityType),
					Codec.INT.optionalFieldOf("stun", 40).forGetter(ProjectileShootingAbility::getStun),
					Codec.DOUBLE.optionalFieldOf("motion", 0.0D).forGetter(ProjectileShootingAbility::getMotion),
					Codec.DOUBLE.optionalFieldOf("acceleration", 0.0D).forGetter(ProjectileShootingAbility::getAcceleration),
					CompoundTag.CODEC.optionalFieldOf("nbt", new CompoundTag()).forGetter(ProjectileShootingAbility::getNbtData),
					SoundInstance.CODEC.optionalFieldOf("sound").forGetter(ProjectileShootingAbility::getSoundInstance)
					).apply(instance, ProjectileShootingAbility::new));
	
	private EntityType<?> projectileEntityType;
	private double motion;
	private double acceleration;
	private CompoundTag nbtData;
	private Optional<SoundInstance> soundInstance;
	
	public ProjectileShootingAbility(EntityType<?> projectileEntityType, int stun, double motion, double acceleration, CompoundTag nbtData, Optional<SoundInstance> soundInstance)
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
	
	public CompoundTag getNbtData()
	{
		return nbtData;
	}
	
	public Optional<SoundInstance> getSoundInstance()
	{
		return soundInstance;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
//			Entity createdEntity = projectileSupplier.apply(player, Vector3d.fromPitchYaw(player.getPitchYaw()));
			
			Entity createdEntity = projectileEntityType.create(player.getCommandSenderWorld());
			
			createdEntity.load(nbtData);
			
			Vec3 dir = Vec3.directionFromRotation(player.getRotationVector());
			
			createdEntity.setDeltaMovement(dir.x * motion, dir.y * motion, dir.z * motion);
			
			createdEntity.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
			
			if(createdEntity instanceof Projectile)
			{
				Projectile proj = (Projectile) createdEntity;
				proj.setOwner(player);
			}
			
			if(createdEntity instanceof AbstractHurtingProjectile)
			{
				AbstractHurtingProjectile dmgProjectile = (AbstractHurtingProjectile) createdEntity;
				
				dmgProjectile.xPower = dir.x * acceleration;
				dmgProjectile.yPower = dir.y * acceleration;
				dmgProjectile.zPower = dir.z * acceleration;
			}
			
			player.level.addFreshEntity(createdEntity);
			stun(player.getUUID());
			
			if(this.soundInstance.isPresent())
				this.soundInstance.get().playSoundAt(player);
		}		
	}
}
