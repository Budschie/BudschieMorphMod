package de.budschie.bmorph.events;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event fires when a player picks up a morph (not by commands). It is only
 * fired on the server.
 **/
public abstract class AcquiredMorphEvent extends Event
{
	private Player player;
	private IMorphCapability morphCapability;
	private MorphItem morph;
	
	public AcquiredMorphEvent(Player player, IMorphCapability morphCapability, MorphItem morph)
	{
		this.player = player;
		this.morphCapability = morphCapability;
		this.morph = morph;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public IMorphCapability getMorphCapability()
	{
		return morphCapability;
	}
	
	public MorphItem getMorph()
	{
		return morph;
	}
	
	/**
	 * This event fires before a player picks up a morph (not by commands). It is only
	 * fired on the server and it can be cancelled.
	 **/
	public static class Pre extends AcquiredMorphEvent
	{
		public Pre(Player player, IMorphCapability morphCapability, MorphItem morph)
		{
			super(player, morphCapability, morph);
		}

		@Override
		public boolean isCancelable()
		{
			return true;
		}
	}

	/**
	 * This event fires after a player picked up a morph (not by commands). It is only
	 * fired on the server and cannot be cancelled.
	 **/
	public static class Post extends AcquiredMorphEvent
	{
		public Post(Player player, IMorphCapability morphCapability, MorphItem morph)
		{
			super(player, morphCapability, morph);
		}

		@Override
		public boolean isCancelable()
		{
			return false;
		}
	}
}
