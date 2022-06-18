package de.budschie.bmorph.morph.functionality;

import java.util.UUID;
import java.util.function.BiConsumer;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.main.ServerSetup;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PassiveTickAbility extends Ability 
{
	private int updateDuration = 0;
	private int lastUpdate = 0;
	private BiConsumer<Player, IMorphCapability> handleUpdate;
	
	public PassiveTickAbility(int updateDuration, BiConsumer<Player, IMorphCapability> handleUpdate)
	{
		this.updateDuration = updateDuration;
		this.handleUpdate = handleUpdate;
	}
	
	public int getUpdateDuration()
	{
		return updateDuration;
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.START)
		{
			int tickCounter = ServerLifecycleHooks.getCurrentServer().getTickCount();
			
			if(tickCounter >= (lastUpdate + updateDuration))
			{
				lastUpdate = tickCounter;
				
				for(UUID uuid : trackedPlayers)
				{
					Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
					
					if(player != null)
					{
						LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
						
						if(cap.isPresent())
						{
							IMorphCapability resolved = cap.resolve().get();
							handleUpdate.accept(player, resolved);
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
