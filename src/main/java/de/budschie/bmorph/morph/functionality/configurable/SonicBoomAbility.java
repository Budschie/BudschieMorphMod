package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class SonicBoomAbility extends StunAbility
{
	public static final Codec<SonicBoomAbility> CODEC = RecordCodecBuilder.create(instance -> 
		instance.group(Codec.INT.fieldOf("stun").forGetter(SonicBoomAbility::getStun))
		.apply(instance, SonicBoomAbility::new));
	
	public SonicBoomAbility(int stun)
	{
		super(stun);
	}

	private static void sonicBoom(Player player, LivingEntity target, ServerLevel serverLevel)
	{
        Vec3 playerPosition = player.position().add(0.0, 1.6, 0.0);
        Vec3 targetPosition = target.getEyePosition().subtract(playerPosition);
        Vec3 direction = targetPosition.normalize();

        for(int i = 1; i < Mth.floor(targetPosition.length()) + 7; ++i) {
           Vec3 particlePosition = playerPosition.add(direction.scale(i));
           serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, particlePosition.x, particlePosition.y, particlePosition.z, 1, 0.0, 0.0, 0.0, 0.0);
        }

        player.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0f, 1.0f);
        target.hurt(serverLevel.damageSources().sonicBoom(player), 10.0f);
        double verticalKnockback = 0.5 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        double horizontalKnockback = 2.5 * (1.0 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        target.push(direction.x() * horizontalKnockback, direction.y() * verticalKnockback, direction.z() * horizontalKnockback);
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(player.level.isClientSide())
		{
			return;
		}
		
		if(isCurrentlyStunned(player.getUUID()))
		{
			return;
		}
		
		Vec3 from = player.position().add(Vec3.directionFromRotation(player.getRotationVector())).add(0, player.getEyeHeight(), 0);
		// Vertical and horizontal distance could be achieved, but I'm too lazy to implement that rn
		Vec3 to = Vec3.directionFromRotation(player.getRotationVector()).scale(10.0).add(from);
		AABB aabb = new AABB(from, to);
		EntityHitResult result = ProjectileUtil.getEntityHitResult(player.level, player, from, to, aabb, entity -> entity instanceof LivingEntity);
		
		if(result != null && result.getEntity() != null && result.getEntity() instanceof LivingEntity livingEntity)
		{
			stun(player.getUUID());
			sonicBoom(player, livingEntity, (ServerLevel) player.level);
		}
	}
}
