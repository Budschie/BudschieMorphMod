package de.budschie.bmorph.morph.functionality.configurable;

import java.util.ArrayList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttackYeetAbility extends Ability
{	
	public static final Codec<AttackYeetAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.optionalFieldOf("yeet_amount", 1.0f).forGetter(AttackYeetAbility::getYeetAmount))
			.apply(instance, AttackYeetAbility::new));
	
	private volatile boolean lock = false;
	ArrayList<Runnable> delayedAttacks = new ArrayList<>();
	
	private float yeetAmount;
	
	public AttackYeetAbility(float yeetAmount)
	{
		this.yeetAmount = yeetAmount;
	}
	
	public float getYeetAmount()
	{
		return yeetAmount;
	}
	
	@SubscribeEvent
	public void onPlayerAttack(LivingDamageEvent event)
	{		
		if (event.getSource() != null && event.getSource().getEntity() instanceof Player && !(event.getEntityLiving() instanceof Player))
		{
			Player attacker = (Player) event.getSource().getEntity();

			if (trackedPlayers.contains(attacker.getUUID()))
			{
				while (lock);
				lock = true;
				
				delayedAttacks.add(() ->
				{
					event.getEntityLiving().setDeltaMovement(event.getEntityLiving().getDeltaMovement().add(0, yeetAmount, 0));
					event.getEntityLiving().level.playSound(null, attacker.blockPosition(),
							SoundEvents.IRON_GOLEM_ATTACK, SoundSource.MASTER, 10, 1);
				});

				lock = false;
			}
		}
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			while(lock);
			lock = true;
			delayedAttacks.forEach(attack -> attack.run());
			delayedAttacks.clear();
			lock = false;
		}
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			while(lock);
			lock = true;
			delayedAttacks.forEach(attack -> attack.run());
			delayedAttacks.clear();
			lock = false;
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
