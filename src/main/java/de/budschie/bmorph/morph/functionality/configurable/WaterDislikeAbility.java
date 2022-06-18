package de.budschie.bmorph.morph.functionality.configurable;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class WaterDislikeAbility extends Ability
{
	public static final Codec<WaterDislikeAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.optionalFieldOf("damage_amount", 1.0f).forGetter(WaterDislikeAbility::getDamageAmount))
			.apply(instance, WaterDislikeAbility::new));
	
	// The damage amount per tick
	private float damageAmount;
	
	public WaterDislikeAbility(float damageAmount)
	{
		this.damageAmount = damageAmount;
	}
	
	public float getDamageAmount()
	{
		return damageAmount;
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			for(UUID playerId : trackedPlayers)
			{
				Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);
				
				if(player.isInWaterRainOrBubble() && trackedPlayers.contains(player.getUUID()))
				{
					player.hurt(DamageSource.DROWN, damageAmount);
				}
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
