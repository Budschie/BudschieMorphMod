package de.budschie.bmorph.render_handler;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

// The fact that I have to do this sh*t shows how horrible some parts of MC code really are...
@EventBusSubscriber(bus = Bus.FORGE, value = Dist.CLIENT)
public class FlyingSynchronizer
{
	@SubscribeEvent
	public static void applyToMorphEntity(PlayerTickEvent event)
	{
		if(event.side == LogicalSide.CLIENT && event.phase == Phase.START)
		{
			Entity morphEntity = RenderHandler.getCachedEntity(event.player);
			
			if(morphEntity instanceof Chicken chicken)
			{
				boolean groundOrFlying = event.player.getAbilities().flying || chicken.isOnGround();
				
				// Assign old values
				chicken.oFlapSpeed = chicken.flapSpeed;
				chicken.oFlap = chicken.flap;
		
				// Change flying speed, accordingly to whether you are flying/on ground or not.
				chicken.flapSpeed += (groundOrFlying ? -1.0f : 4.0f) * 0.3f;
				chicken.flapSpeed = Mth.clamp(chicken.flapSpeed, 0.0f, 1.0f);
				
				if (!groundOrFlying && chicken.flapping < 1.0f)
				{
					chicken.flapping = 1.0f;
				}
		
				chicken.flapping = chicken.flapping * 0.9f;
		
				chicken.flap += chicken.flapping * 2.0f;
			}
			else if(morphEntity instanceof Parrot parrot)
			{
				parrot.calculateFlapping();
			}
		}
	}
}
