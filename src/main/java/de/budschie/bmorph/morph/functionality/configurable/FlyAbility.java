package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FlyAbility extends AbstractEventAbility
{
	public static final Codec<FlyAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("flying_speed", 0.05f).forGetter(FlyAbility::getFlyingSpeed))
			.apply(instance, FlyAbility::new));
	
	private float flyingSpeed;
	
	public FlyAbility(float flyingSpeed)
	{
		this.flyingSpeed = flyingSpeed;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
		super.enableAbility(player, enabledItem);

		player.getAbilities().mayfly = true;
		player.getAbilities().setFlyingSpeed(flyingSpeed);
	}

	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		if(!player.isCreative() && !player.isSpectator())
		{
			player.getAbilities().setFlyingSpeed(0.05f);
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
		}

		super.disableAbility(player, disabledItem);
	}
	
	@SubscribeEvent
	public void onPlayerTickEvent(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			if(trackedPlayers.contains(event.player.getUUID()) && event.player.getAbilities().flying)
			{
				event.player.setPose(Pose.STANDING);
				event.player.setForcedPose(null);
				event.player.getAbilities().mayfly = true;
			}
		}
	}
	
	public float getFlyingSpeed()
	{
		return flyingSpeed;
	}
}
