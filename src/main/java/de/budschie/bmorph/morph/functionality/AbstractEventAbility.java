package de.budschie.bmorph.morph.functionality;

import java.util.HashSet;
import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public abstract class AbstractEventAbility extends Ability 
{
	protected HashSet<UUID> trackedPlayers = new HashSet<>();
	
	public AbstractEventAbility()
	{
		
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
		trackedPlayers.add(player.getUUID());
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem)
	{
		trackedPlayers.remove(player.getUUID());
	}
	
	@Override
	public void onRegister()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@Override
	public void onUnregister()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	public boolean isTracked(Entity entity)
	{
		return trackedPlayers.contains(entity.getUUID());
	}
}
