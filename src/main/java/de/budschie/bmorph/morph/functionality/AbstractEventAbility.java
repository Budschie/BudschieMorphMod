package de.budschie.bmorph.morph.functionality;

import java.util.HashSet;
import java.util.UUID;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;

public abstract class AbstractEventAbility extends Ability 
{
	protected HashSet<UUID> trackedPlayers = new HashSet<>();
	
	public AbstractEventAbility()
	{
		
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
		return trackedPlayers.contains(entity.getUniqueID());
	}
}
