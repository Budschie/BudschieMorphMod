package de.budschie.bmorph.render_handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class InitializeMorphEntityEvent extends Event
{
	private PlayerEntity player;
	private Entity morphEntity;
	
	public InitializeMorphEntityEvent(PlayerEntity player, Entity morphEntity)
	{
		this.player = player;
		this.morphEntity = morphEntity;
	}
	
	public PlayerEntity getPlayer()
	{
		return player;
	}
	
	public Entity getMorphEntity()
	{
		return morphEntity;
	}
}
