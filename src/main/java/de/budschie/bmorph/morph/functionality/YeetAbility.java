package de.budschie.bmorph.morph.functionality;

import java.util.ArrayList;
import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class YeetAbility extends AbstractEventAbility
{
	ArrayList<Runnable> delayedAttacks = new ArrayList<>();
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
	
	@SubscribeEvent
	public void onPlayerAttack(LivingAttackEvent event)
	{
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof PlayerEntity)
		{
			PlayerEntity attacker = (PlayerEntity) event.getSource().getTrueSource();
			
			if(trackedPlayers.contains(attacker.getUniqueID()))
			{
				delayedAttacks.add(() ->
				{
					event.getEntityLiving().setMotion(event.getEntityLiving().getMotion().add(0, .65f, 0));
					event.getEntityLiving().world.playSound(null, attacker.getPosition(), SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.MASTER, 10, 1);
				});
			}
		}
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		delayedAttacks.forEach(attack -> attack.run());
		delayedAttacks.clear();
	}
	
	@Override
	public void onServerStopped(FMLServerStoppingEvent event)
	{
		delayedAttacks.clear();
		super.onServerStopped(event);
	}
}
