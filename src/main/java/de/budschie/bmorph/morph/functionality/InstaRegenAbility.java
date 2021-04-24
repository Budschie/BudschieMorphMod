package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InstaRegenAbility extends AbstractEventAbility
{
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
	}
	
	@SubscribeEvent
	public void onEat(LivingEntityUseItemEvent.Finish event)
	{
		if(trackedPlayers.contains(event.getEntityLiving().getUniqueID()))
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			if(event.getItem().isFood())
			{
				player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 4));
			}
		}
	}
}
