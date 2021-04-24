package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoKnockbackAbility extends AbstractEventAbility
{
	@SubscribeEvent
	public void onLivingKnockbackEvent(LivingKnockBackEvent event)
	{
		if(trackedPlayers.contains(event.getEntityLiving().getUniqueID()))
				event.setCanceled(true);
	}
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
