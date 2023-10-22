package de.budschie.bmorph.capabilities;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event fires when the MorphStateMachine recorded a change of state which can not be traced back to deserialization or synchronization.
 */
public class MorphStateChangeEvent extends Event
{
	public static class MorphStateChange
	{
		private String stateKey;
		private Optional<String> oldValue;
		private Optional<String> newValue;
		
		public MorphStateChange(String stateKey, Optional<String> oldValue, Optional<String> newValue)
		{
			this.stateKey = stateKey;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		public String getStateKey()
		{
			return stateKey;
		}
		
		public Optional<String> getOldValue()
		{
			return oldValue;
		}
		
		public Optional<String> getNewValue()
		{
			return newValue;
		}
	}
	
	private MorphStateChange morphStateChange;
	private Player player;
	
	public MorphStateChangeEvent(MorphStateChange morphStateChange, Player player)
	{
		this.morphStateChange = morphStateChange;
		this.player = player;
	}
	
	public MorphStateChange getMorphStateChange()
	{
		return morphStateChange;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	@Override
	public boolean isCancelable()
	{
		return false;
	}
}
