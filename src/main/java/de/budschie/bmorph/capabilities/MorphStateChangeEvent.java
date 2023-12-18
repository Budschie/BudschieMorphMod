package de.budschie.bmorph.capabilities;

import java.util.Optional;

import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event fires when the MorphStateMachine recorded a change of state which can not be traced back to deserialization or synchronization.
 */
public class MorphStateChangeEvent extends Event
{
	public static class MorphStateChange
	{
		private ResourceLocation stateKey;
		private Optional<MorphStateMachineEntry> oldValue;
		private Optional<MorphStateMachineEntry> newValue;
		
		public MorphStateChange(ResourceLocation stateKey, Optional<MorphStateMachineEntry> oldValue, Optional<MorphStateMachineEntry> newValue)
		{
			this.stateKey = stateKey;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		public ResourceLocation getStateKey()
		{
			return stateKey;
		}
		
		public Optional<MorphStateMachineEntry> getOldValue()
		{
			return oldValue;
		}
		
		public Optional<MorphStateMachineEntry> getNewValue()
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
