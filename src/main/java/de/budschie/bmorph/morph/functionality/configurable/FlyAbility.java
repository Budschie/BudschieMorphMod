package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.capabilities.AbilitySerializationContext.AbilitySerializationObject;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.Flight;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

public class FlyAbility extends Ability
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
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		player.getAbilities().mayfly = true;
		player.getAbilities().setFlyingSpeed(flyingSpeed);
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		boolean mayStillFly = false;
		
		abilitySearch:
		for(Ability ability : newAbilities)
		{
			if(ability instanceof FlyAbility)
			{
				mayStillFly = true;
				break abilitySearch;
			}
		}
		
		if(!player.isCreative() && !player.isSpectator() && !mayStillFly)
		{
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
		}
		
		player.getAbilities().setFlyingSpeed(0.05f);
	}
	
	@SubscribeEvent
	public void onPlayerTickEvent(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			if(trackedPlayers.contains(event.player.getUUID()))
			{
				if(event.player.getAbilities().flying)
				{
					event.player.setPose(Pose.STANDING);
					event.player.setForcedPose(null);
					
					// Give player full speed when flying in creative
					if(event.player.isCreative() || event.player.isSpectator())
						event.player.getAbilities().setFlyingSpeed(0.05F);
					else
						event.player.getAbilities().setFlyingSpeed(flyingSpeed);
				}
				
				if(!event.player.getAbilities().mayfly)
					event.player.getAbilities().mayfly = true;
			}
		}
	}
	
	@Override
	public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
	{
		super.serialize(player, context, canSaveTransientData);
		
		if(canSaveTransientData)
			context.getOrCreateSerializationObjectForAbility(this).createTransientTag().putBoolean("player_flying", player.getAbilities().flying);
	}
	
	@Override
	public void deserialize(Player player, AbilitySerializationContext context)
	{
		super.deserialize(player, context);
		
		AbilitySerializationObject object = context.getSerializationObjectForAbilityOrNull(this);
		
		if(object != null && object.getTransientTag().isPresent() && object.getTransientTag().get().contains("player_flying"))
		{
			player.getAbilities().flying = player.getAbilities().flying || object.getTransientTag().get().getBoolean("player_flying");
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new Flight.FlightPacket(player.getAbilities().flying));
		}
	}
	
	public float getFlyingSpeed()
	{
		return flyingSpeed;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
