package de.budschie.bmorph.events;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.entity.MorphEntity;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class Events
{
	@SubscribeEvent
	public static void onPlayerJoined(PlayerLoggedInEvent event)
	{
		if(!event.getEntity().world.isRemote)
		{
			ServerSetup.server.getPlayerList().getPlayers().forEach(player -> 
			{
				LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					cap.resolve().get().syncWithClient(player, (ServerPlayerEntity) event.getEntity());
				}
			});
		}
	}
	
	@SubscribeEvent
	public static void onPlayerKilledLivingEntity(LivingDeathEvent event)
	{
		if(!event.getEntity().world.isRemote)
		{
			if(event.getSource().getTrueSource() instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
				
				LazyOptional<IMorphCapability> playerMorph = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(playerMorph.isPresent())
				{
					MorphItem morphItem = MorphManagerHandlers.createMorphFromDeadEntity(event.getEntity());
					
					if(morphItem != null)
					{
						IMorphCapability resolved = playerMorph.resolve().get();
						
						if(!resolved.getMorphList().contains(morphItem))
						{
							MorphEntity morphEntity = new MorphEntity(event.getEntity().world, morphItem);
							morphEntity.forceSetPosition(event.getEntity().getPosX(), event.getEntity().getPosY(), event.getEntity().getPosZ());
							event.getEntity().world.addEntity(morphEntity);
							
							System.out.println("Spawned entity!");
						}
					}
				}
			}
		}
	}
}
