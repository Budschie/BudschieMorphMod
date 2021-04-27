package de.budschie.bmorph.morph.functionality;

import java.util.ArrayList;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class AttackYeetAbility extends AbstractEventAbility
{
	private volatile boolean lock = false;
	ArrayList<Runnable> delayedAttacks = new ArrayList<>();
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		super.enableAbility(player, enabledItem);
	}
	
	@SubscribeEvent
	public void onPlayerAttack(LivingDamageEvent event)
	{		
		if (event.getSource() != null && event.getSource().getTrueSource() instanceof PlayerEntity && !(event.getEntityLiving() instanceof PlayerEntity))
		{
			PlayerEntity attacker = (PlayerEntity) event.getSource().getTrueSource();

			if (trackedPlayers.contains(attacker.getUniqueID()))
			{
				while (lock);
				lock = true;

				delayedAttacks.add(() ->
				{
					event.getEntityLiving().setMotion(event.getEntityLiving().getMotion().add(0, 1, 0));
					event.getEntityLiving().world.playSound(null, attacker.getPosition(),
							SoundEvents.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.MASTER, 10, 1);
				});

				lock = false;
			}
		}
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		while(lock);
		lock = true;
		delayedAttacks.forEach(attack -> attack.run());
		delayedAttacks.clear();
		lock = false;
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		while(lock);
		lock = true;
		delayedAttacks.forEach(attack -> attack.run());
		delayedAttacks.clear();
		lock = false;
	}
	
	@Override
	public void onServerStopped(FMLServerStoppingEvent event)
	{
		delayedAttacks.clear();
		super.onServerStopped(event);
	}
}
