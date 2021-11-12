package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class InitializeMorphEntityEvent extends Event
{
	private Player player;
	private Entity morphEntity;
	
	public InitializeMorphEntityEvent(Player player, Entity morphEntity)
	{
		this.player = player;
		this.morphEntity = morphEntity;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public Entity getMorphEntity()
	{
		return morphEntity;
	}
}
