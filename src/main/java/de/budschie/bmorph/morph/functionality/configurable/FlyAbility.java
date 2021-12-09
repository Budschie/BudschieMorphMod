package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FlyAbility extends AbstractEventAbility
{
	public static final Codec<FlyAbility> CODEC = ModCodecs.newCodec(FlyAbility::new);
	
	public FlyAbility()
	{
		
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
		super.enableAbility(player, enabledItem);

		player.getAbilities().mayfly = true;
	}

	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		if(!player.isCreative() && !player.isSpectator())
		{
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
}
