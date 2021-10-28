package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public class ProjectileShootingAbility extends StunAbility
{
	public static Codec<ProjectileShootingAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
					ModCodecs.ENTITIES.fieldOf("projectile_entity").forGetter(ProjectileShootingAbility::getProjectileEntityType),
					Codec.INT.optionalFieldOf("stun", 40).forGetter(ProjectileShootingAbility::getStun),
					Codec.DOUBLE.optionalFieldOf("force", 2.0D).forGetter(ProjectileShootingAbility::getForce),
					CompoundNBT.CODEC.optionalFieldOf("nbt", new CompoundNBT()).forGetter(ProjectileShootingAbility::getNbtData)
					).apply(instance, ProjectileShootingAbility::new));
	
	private EntityType<?> projectileEntityType;
	private double force;
	private CompoundNBT nbtData;
	
	public ProjectileShootingAbility(EntityType<?> projectileEntityType, int stun, double force, CompoundNBT nbtData)
	{
		super(stun);
		this.force = force;
		this.projectileEntityType = projectileEntityType;
		this.nbtData = nbtData;
	}
	
	public EntityType<?> getProjectileEntityType()
	{
		return projectileEntityType;
	}
	
	public double getForce()
	{
		return force;
	}
	
	public CompoundNBT getNbtData()
	{
		return nbtData;
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
			
			createdEntity.setMotion(dir.x * force, dir.y * force, dir.z * force);
			
			createdEntity.setPosition(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
			player.world.addEntity(createdEntity);
			stun(player.getUniqueID());
		}		
	}
}
