package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;
import java.util.UUID;

import de.budschie.bmorph.main.ServerSetup;

public abstract class StunAbility extends Ability
{
	private int stun;
	private HashMap<UUID, Integer> delayHashMap = new HashMap<>();
	
	public StunAbility(int stun)
	{
		this.stun = stun;
	}
	
	public int getStun()
	{
		return stun;
	}
	
	public boolean isCurrentlyStunned(UUID player)
	{
		return (delayHashMap.getOrDefault(player, 0) + stun) > ServerSetup.server.getTickCount();
	}
	
	public void stun(UUID player)
	{
		delayHashMap.put(player, ServerSetup.server.getTickCount());
	}
	
//	@SubscribeEvent
//	public void onServerStop(FMLServerStoppingEvent event)
//	{
//		delayHashMap.clear();
//		System.out.println("Cleared stun list for " + this.getClass().getName() + ".");
//	}
}
