package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FlyAbility extends AbstractEventAbility
{
	public static Codec<FlyAbility> CODEC = ModCodecs.newCodec(FlyAbility::new);
	
	public FlyAbility()
	{
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		player.abilities.allowFlying = true;
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		if(!player.isCreative() && !player.isSpectator())
		{
			player.abilities.allowFlying = false;
			player.abilities.isFlying = false;
		}
	}
	
	@SubscribeEvent
	public void onPlayerTickEvent(PlayerTickEvent event)
	{
		if(trackedPlayers.contains(event.player.getUniqueID()) && event.player.abilities.isFlying)
		{
			event.player.setPose(Pose.STANDING);
			event.player.setForcedPose(null);
			event.player.abilities.allowFlying = true;
		}
	}
}
