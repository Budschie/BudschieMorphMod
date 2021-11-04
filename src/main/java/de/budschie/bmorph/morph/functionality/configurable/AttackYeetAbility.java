package de.budschie.bmorph.morph.functionality.configurable;

import java.util.ArrayList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttackYeetAbility extends AbstractEventAbility
{	
	public static final Codec<AttackYeetAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.FLOAT.optionalFieldOf("yeetAmount", 1.0f).forGetter(AttackYeetAbility::getYeetAmount))
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
		if (event.getSource() != null && event.getSource().getTrueSource() instanceof PlayerEntity && !(event.getEntityLiving() instanceof PlayerEntity))
		{
			PlayerEntity attacker = (PlayerEntity) event.getSource().getTrueSource();

			if (trackedPlayers.contains(attacker.getUniqueID()))
			{
				while (lock);
				lock = true;
				
				delayedAttacks.add(() ->
				{
					event.getEntityLiving().setMotion(event.getEntityLiving().getMotion().add(0, yeetAmount, 0));
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
}
