package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public class ProjectileShootingAbility extends StunAbility
{
	public static final Codec<ProjectileShootingAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					ModCodecs.ENTITIES.fieldOf("projectile_entity").forGetter(ProjectileShootingAbility::getProjectileEntityType),
					Codec.INT.optionalFieldOf("stun", 0).forGetter(ProjectileShootingAbility::getStun),
					Codec.DOUBLE.optionalFieldOf("motion", 0.0D).forGetter(ProjectileShootingAbility::getMotion),
					CompoundTag.CODEC.optionalFieldOf("nbt", new CompoundTag()).forGetter(ProjectileShootingAbility::getNbtData),
					AudioVisualEffect.CODEC.optionalFieldOf("audiovisual_effect").forGetter(ProjectileShootingAbility::getAudioVisualEffect)
					).apply(instance, ProjectileShootingAbility::new));
	
	private EntityType<?> projectileEntityType;
	private double motion;
	private CompoundTag nbtData;
	private Optional<AudioVisualEffect> audioVisualEffect;
	
	public ProjectileShootingAbility(EntityType<?> projectileEntityType, int stun, double motion, CompoundTag nbtData, Optional<AudioVisualEffect> audioVisualEffect)
	{
		super(stun);
		this.motion = motion;
		this.projectileEntityType = projectileEntityType;
		this.nbtData = nbtData;
		this.audioVisualEffect = audioVisualEffect;
	}
	
	public EntityType<?> getProjectileEntityType()
	{
		return projectileEntityType;
	}
	
	public double getMotion()
	{
		return motion;
	}
	
	public CompoundTag getNbtData()
	{
		return nbtData;
	}
	
	public Optional<AudioVisualEffect> getAudioVisualEffect()
	{
		return audioVisualEffect;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
//			Entity createdEntity = projectileSupplier.apply(player, Vector3d.fromPitchYaw(player.getPitchYaw()));
			
			Entity createdEntity = projectileEntityType.create(player.level);
						
			createdEntity.load(nbtData);
						
			Vec3 dir = Vec3.directionFromRotation(player.getRotationVector()).normalize();
			
			createdEntity.setDeltaMovement(dir.x * motion, dir.y * motion, dir.z * motion);
			createdEntity.moveTo(player.getX(), player.getY() + player.getEyeHeight(), player.getZ(), player.getYHeadRot(), player.getXRot());
			
			if(createdEntity instanceof Projectile proj)
			{
				proj.setOwner(player);			
			}
						
			if(createdEntity instanceof AbstractHurtingProjectile dmgProjectile)
			{
				dmgProjectile.xPower = dir.x * 0.1D;
				dmgProjectile.yPower = dir.y * 0.1D;
				dmgProjectile.zPower = dir.z * 0.1D;
			}			
			
			player.level.addFreshEntity(createdEntity);					

			
			stun(player.getUUID());
			
			this.audioVisualEffect.ifPresent(ave -> ave.playEffect(player));
		}		
	}
}
