package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EnderDragonAbility extends Ability
{
	public static final Codec<EnderDragonAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group
	(
		Codec.DOUBLE.fieldOf("acceleration").forGetter(EnderDragonAbility::getAcceleration),
		Codec.DOUBLE.fieldOf("max_speed").forGetter(EnderDragonAbility::getMaxSpeed)
	).apply(instance, EnderDragonAbility::new));
	
	private double acceleration;
	private double maxSpeed;
	
	public EnderDragonAbility(double acceleration, double maxSpeed)
	{
		this.acceleration = acceleration;
		this.maxSpeed = maxSpeed;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		player.setNoGravity(true);
		player.getAbilities().mayfly = true;
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		player.setNoGravity(false);
		player.getAbilities().mayfly = false;
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END && isTracked(event.player))
		{
			if(!event.player.isCrouching())
				event.player.setDeltaMovement(event.player.getDeltaMovement().add(event.player.getForward().multiply(acceleration, acceleration, acceleration)));
			
			if(event.player.getDeltaMovement().lengthSqr() > (maxSpeed * maxSpeed))
			{
				event.player.setDeltaMovement(event.player.getForward().multiply(maxSpeed, maxSpeed, maxSpeed));
			}
			
			// Thou shalt not fly
			event.player.getAbilities().flying = false;
			// Thou shalt not be kicked
			if(!event.player.getAbilities().mayfly)
			{
				event.player.getAbilities().mayfly = true;
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	public void setAcceleration(float acceleration)
	{
		this.acceleration = acceleration;
	}
	
	public double getAcceleration()
	{
		return acceleration;
	}
	
	public void setMaxSpeed(double maxSpeed)
	{
		this.maxSpeed = maxSpeed;
	}
	
	public double getMaxSpeed()
	{
		return maxSpeed;
	}
}
