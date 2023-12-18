package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoInteractAbility extends Ability
{
	public static Codec<NoInteractAbility> CODEC = Codec.unit(NoInteractAbility::new);
	
	public NoInteractAbility()
	{
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event)
	{
		if(!isTracked(event.getEntity()))
		{
			return;
		}
		
		event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onAttack(LivingAttackEvent event)
	{
		if(!(event.getSource().getEntity() instanceof Player player))
		{
			return;
		}
		
		if(!isTracked(player))
		{
			return;
		}
		
		event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void onMine(PlayerEvent.BreakSpeed event)
	{
		if(!isTracked(event.getEntity()))
		{
			return;
		}
		
		event.setNewSpeed(0.0f);
	}
	
	@SubscribeEvent
	public void onPlace(EntityPlaceEvent event)
	{
		if(!(event.getEntity() instanceof Player player))
		{
			return;
		}
		
		if(!isTracked(player))
		{
			return;
		}
		
		event.setCanceled(true);
	}
}
