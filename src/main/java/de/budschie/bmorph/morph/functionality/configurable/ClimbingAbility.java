package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClimbingAbility extends Ability
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
		if(event.phase == Phase.START)
		{
			Player player = event.player;
	
			if (trackedPlayers.contains(player.getUUID()) && player.horizontalCollision && !player.getAbilities().flying)
			{
				Vec3 toSet = player.getDeltaMovement().add(0, getClimbingSpeedAcceleration(), 0);
				player.setDeltaMovement(new Vec3(toSet.x, Math.min(toSet.y, getMaxClimbingSpeed()), toSet.z));
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
