package de.budschie.bmorph.capabilities.phantom_glide;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class GlideStatusChangedEvent extends Event
{
	private Player player;
	private GlideStatus oldGlideStatus;
	private GlideStatus newGlideStatus;
	
	public GlideStatusChangedEvent(Player player, GlideStatus oldGlideStatus, GlideStatus newGlideStatus)
	{
		this.player = player;
		this.oldGlideStatus = oldGlideStatus;
		this.newGlideStatus = newGlideStatus;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public GlideStatus getOldGlideStatus()
	{
		return oldGlideStatus;
	}
	
	public GlideStatus getNewGlideStatus()
	{
		return newGlideStatus;
	}
}
