package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WitherEffectOnHitAbility extends AbstractEventAbility
{
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
	
	@SubscribeEvent
	public void onDamagedEntity(LivingDamageEvent event)
	{
		if(event.getSource().getTrueSource() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
			
			if(trackedPlayers.contains(player.getUniqueID()))
			{
				event.getEntityLiving().addPotionEffect(new EffectInstance(Effects.WITHER, 250, 2));
			}
		}
	}
}
