package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;
import java.util.UUID;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public abstract class StunAbility extends Ability
{
	private int stun;
	private HashMap<UUID, Integer> delayHashMap = new HashMap<>();
	
	public StunAbility(int stun)
	{
		this.stun = stun;
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public boolean isCurrentlyStunned(UUID player)
	{
		return (delayHashMap.getOrDefault(player, 0) + stun) > ServerSetup.server.getTickCounter();
	}
	
	public void stun(UUID player)
	{
		delayHashMap.put(player, ServerSetup.server.getTickCounter());
	}
	
	@SubscribeEvent
	public void onServerStop(FMLServerStoppingEvent event)
	{
		delayHashMap.clear();
		System.out.println("Cleared stun list for " + this.getClass().getName() + ".");
	}
}
