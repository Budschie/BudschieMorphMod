package de.budschie.bmorph.morph.functionality;

import java.util.HashSet;
import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public abstract class AbstractEventAbility extends Ability 
{
	protected HashSet<UUID> trackedPlayers = new HashSet<>();
	
	public AbstractEventAbility()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		trackedPlayers.add(player.getUniqueID());
	}
	
	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		trackedPlayers.remove(player.getUniqueID());
	}
	
	@SubscribeEvent
	public void onServerStopped(FMLServerStoppingEvent event)
	{
		trackedPlayers.clear();
		System.out.println("Clearing player list of passive ability " + this.getClass().getName() + "...");
	}
}
