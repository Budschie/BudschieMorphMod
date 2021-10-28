package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;

public class ClimbingAbility extends AbstractEventAbility
{
	public static final Codec<ClimbingAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.optionalFieldOf("climbing_speed_acceleration", .1f).forGetter(ClimbingAbility::getClimbingSpeedAcceleration),
					Codec.FLOAT.optionalFieldOf("max_climbing_speed", .2f).forGetter(ClimbingAbility::getMaxClimbingSpeed))
			.apply(instance, ClimbingAbility::new));
	
	private float climbingSpeedAcceleration;
	private float maxClimbingSpeed;
	
	/** Maybe we can add a block predicate later to only allow some blocks that should be climbable. **/
	public ClimbingAbility(float climbingSpeedAcceleration, float maxClimbingSpeed)
	{
		this.climbingSpeedAcceleration = climbingSpeedAcceleration;
		this.maxClimbingSpeed = maxClimbingSpeed;
	}
	
	public float getClimbingSpeedAcceleration()
	{
		return climbingSpeedAcceleration;
	}
	
	public float getMaxClimbingSpeed()
	{
		return maxClimbingSpeed;
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		PlayerEntity player = event.player;

		if (trackedPlayers.contains(player.getUniqueID()) && player.collidedHorizontally && !player.abilities.isFlying)
		{
			Vector3d toSet = player.getMotion().add(0, getClimbingSpeedAcceleration(), 0);
			player.setMotion(new Vector3d(toSet.x, Math.min(toSet.y, getMaxClimbingSpeed()), toSet.z));
		}
	}
}
