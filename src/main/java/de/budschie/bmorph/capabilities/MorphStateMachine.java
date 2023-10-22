package de.budschie.bmorph.capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import de.budschie.bmorph.network.MorphStateMachineChangedSync.MorphStateMachineChangedSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class MorphStateMachine
{
	public static class MorphStateMachineRecordedChanges
	{
		private HashMap<String, String> changes;
		private Player player;
		// FIXME: Investigate edge case where setting the morph state machine via IMorphCapability#setMorphStateMachine whilst still having a state change recorded may cause changes.
		private MorphStateMachine morphStateMachine;
		
		MorphStateMachineRecordedChanges(Player player, MorphStateMachine morphStateMachine, HashMap<String, String> changes)
		{
			this.player = player;
			this.morphStateMachine = morphStateMachine;
			this.changes = changes;
		}
		
		// C++ move constructor but bad
		MorphStateMachineRecordedChanges(MorphStateMachineChangeRecorder recorder)
		{
			// Ref-copy elements over
			this.changes = recorder.changes;
			this.player = recorder.player;
			this.morphStateMachine = recorder.morphStateMachine;
			
			// Invalidate original state machine recorder
			recorder.changes = null;
			recorder.player = null;
			recorder.morphStateMachine = null;
		}
		
		private boolean areDifferent(String key)
		{
			return changes.get(key) != morphStateMachine.states.get(key);
		}
		
		public void applyChanges()
		{
			// Fire event
			for(Entry<String, String> change : changes.entrySet())
			{
				// If a change indeed happened
				if(areDifferent(change.getKey()))
				{
					MorphStateChangeEvent.MorphStateChange stateDifference = 
							new MorphStateChangeEvent.MorphStateChange(change.getKey(), Optional.ofNullable(this.morphStateMachine.states.get(change.getKey())), Optional.ofNullable(change.getValue()));
					
					MorphStateChangeEvent event = new MorphStateChangeEvent(stateDifference, this.player);
					MinecraftForge.EVENT_BUS.post(event);
				}
			}
			
			for(Entry<String, String> change : changes.entrySet())
			{
				// Clear the state if a change of a key to the value null has been recorded.
				if(change.getValue() == null)
				{
					this.morphStateMachine.removeStateEventUnaware(change.getKey());
				}
				else
				{
					this.morphStateMachine.setStateEventUnaware(change.getKey(), change.getValue());
				}
			}
		}
		
		MorphStateMachineChangedSyncPacket createNetworkPacket()
		{
			return new MorphStateMachineChangedSyncPacket(changes, this.player.getUUID());
		}
	}
	
	public static class MorphStateMachineChangeRecorder
	{
		private HashMap<String, String> changes;
		private Player player;
		private MorphStateMachine morphStateMachine;
		
		MorphStateMachineChangeRecorder(Player player, MorphStateMachine morphStateMachine)
		{
			this.player = player;
			this.morphStateMachine = morphStateMachine;
		}
		
		public MorphStateMachineChangeRecorder recordChange(String stateKey, String stateValue)
		{
			changes.put(stateKey, stateValue);
			return this;
		}
		
		public MorphStateMachineChangeRecorder recordChange(String stateKey)
		{
			return recordChange(stateKey, null);
		}
		
		/**
		 * Finish recording. This will invalidate this object.
		 * @return a MorphStateMachineRecordedChanges object, which is a read-only structure to prevent accidental records after applying or synchronizing this object.
		 */
		public MorphStateMachineRecordedChanges finishRecording()
		{
			return new MorphStateMachineRecordedChanges(this);
		}
	}
	
	// TODO: Add change reasons so that we may fire events when states change, but only when they do so through abilities or sth like that
	private HashMap<String, String> states = new HashMap<>();
	
	public MorphStateMachine()
	{
		
	}
		
	/**
	 * Clears every stateKey and stateValue.
	 */
	public void clear()
	{
		states.clear();
	}
	
	/**
	 * Query the morph state machine. Either receive an empty optional,
	 * or an optional containing the stateValue.
	 * @param stateKey
	 * @return the stateValue.
	 */
	public Optional<String> query(String stateKey)
	{
		return Optional.ofNullable(states.get(stateKey));
	}
			
	private void setStateEventUnaware(String stateKey, String stateValue)
	{
		states.put(stateKey, stateValue);
	}
	
	private void removeStateEventUnaware(String stateKey)
	{
		states.remove(stateKey);
	}
	
	public void deserialize(CompoundTag tag)
	{
		clear();
		ListTag stateEntries = tag.getList("states", Tag.TAG_COMPOUND);
		
		for(int i = 0; i < stateEntries.size(); i++)
		{
			CompoundTag stateEntry = stateEntries.getCompound(i);
			setStateEventUnaware(stateEntry.getString("stateKey"), stateEntry.getString("stateValue"));
		}
	}
	
	public CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();
		ListTag stateEntries = new ListTag();
		
		tag.put("states", stateEntries);
		
		for(Map.Entry<String, String> stateEntry : states.entrySet())
		{
			CompoundTag entryTag = new CompoundTag();
			entryTag.putString("stateKey", stateEntry.getKey());
			entryTag.putString("stateValue", stateEntry.getValue());
			
			stateEntries.add(entryTag);
		}
		
		return tag;
	}
	
	public void deserializePacket(FriendlyByteBuf buf)
	{
		clear();
		int size = buf.readInt();
		
		for(int i = 0; i < size; i++)
		{
			states.put(buf.readUtf(), buf.readUtf());
		}
	}
	
	public void serializePacket(FriendlyByteBuf buf)
	{
		buf.writeInt(states.size());
		
		for(Map.Entry<String, String> stateEntry : states.entrySet())
		{
			buf.writeUtf(stateEntry.getKey());
			buf.writeUtf(stateEntry.getValue());
		}
	}
}
