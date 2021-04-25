package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

public class TeleportAbility extends StunAbility
{
	public TeleportAbility(int stun)
	{
		super(stun);
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
			RayTraceContext context = new RayTraceContext(player.getPositionVec().add(Vector3d.fromPitchYaw(player.getPitchYaw())).add(0, player.getEyeHeight(), 0), player.getPositionVec().mul(Vector3d.fromPitchYaw(player.getPitchYaw()).mul(10, 10, 10)).add(0, player.getEyeHeight(), 0), BlockMode.VISUAL, FluidMode.SOURCE_ONLY, null);
			
			BlockRayTraceResult result = player.world.rayTraceBlocks(context);
			
			// We are looking for a length of 25 (25^2=625)
			if(result.getType() == RayTraceResult.Type.BLOCK && player.getPositionVec().subtract(result.getHitVec()).lengthSquared() < 50*50)
			{
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 10, .7f);
				((ServerWorld)player.world).spawnParticle(ParticleTypes.PORTAL, player.getPosX(), player.getPosY(), player.getPosZ(), 300, 1, 1, 1, 0);
				player.teleportKeepLoaded(result.getHitVec().getX(), result.getHitVec().getY() + 1, result.getHitVec().getZ());
				player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 10, 2f);
				
				stun(player.getUniqueID());
			}
		}
	}
}
