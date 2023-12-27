package de.budschie.bmorph.capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import de.budschie.bmorph.network.MorphStateMachineChangedSync.MorphStateMachineChangedSyncPacket;
import de.budschie.bmorph.util.TickTimestamp;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class MorphStateMachine
{
	public static class MorphStateMachineEntry
	{
		private Optional<TickTimestamp> timeElapsed;
		private Optional<String> value;
		
		public MorphStateMachineEntry(Optional<TickTimestamp> timeElapsedSinceChange, Optional<String> value)
		{
			this.timeElapsed = timeElapsedSinceChange;
			this.value = value;
		}

		public Optional<TickTimestamp> getTimeElapsedSinceChange()
		{
			return timeElapsed;
		}
		
		public Optional<String> getValue()
		{
			return value;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof MorphStateMachineEntry entry))
			{
				return false;
			}
			
			return entry.getValue().equals(this.getValue()) && entry.getTimeElapsedSinceChange().equals(this.getTimeElapsedSinceChange());
		}
	}
	
	public static class MorphStateMachineRecordedChanges
	{
		private HashMap<ResourceLocation, MorphStateMachineEntry> changes;
		private Player player;
		// FIXME: Investigate edge case where setting the morph state machine via IMorphCapability#setMorphStateMachine whilst still having a state change recorded may cause changes.
		private MorphStateMachine morphStateMachine;
		
		MorphStateMachineRecordedChanges(Player player, MorphStateMachine morphStateMachine, HashMap<ResourceLocation, MorphStateMachineEntry> changes)
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
		
		private boolean areDifferent(ResourceLocation key)
		{
			MorphStateMachineEntry change = changes.get(key);
			MorphStateMachineEntry current = morphStateMachine.states.get(key);
			
			// Both are the same => return false
			if(change == current)
			{
				return false;
			}
			
			// Change is null, other is not => return true
			if(change == null)
			{
				return true;
			}
			
			// Do more sophisitcated checks
			return !change.equals(current);
		}
		
		public void applyChanges()
		{
			// Fire event
			for(Entry<ResourceLocation, MorphStateMachineEntry> change : changes.entrySet())
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
			
			for(Entry<ResourceLocation, MorphStateMachineEntry> change : changes.entrySet())
			{
				this.morphStateMachine.setStateEventUnaware(change.getKey(), change.getValue());
			}
		}
		
		MorphStateMachineChangedSyncPacket createNetworkPacket()
		{
			return MorphStateMachineChangedSyncPacket.fromChanges(changes, this.player.getUUID());
		}
	}
	
	public static class MorphStateMachineChangeRecorder
	{
		private HashMap<ResourceLocation, MorphStateMachineEntry> changes = new HashMap<>();
		private Player player;
		private MorphStateMachine morphStateMachine;
		
		MorphStateMachineChangeRecorder(Player player, MorphStateMachine morphStateMachine)
		{
			this.player = player;
			this.morphStateMachine = morphStateMachine;
		}
		
		// NOT THREAD SAFE
		public MorphStateMachineChangeRecorder recordChange(ResourceLocation stateKey, MorphStateMachineEntry stateValue)
		{
			changes.put(stateKey, stateValue);
			return this;
		}
		
		// NOT THREAD SAFE
		public MorphStateMachineChangeRecorder recordChange(ResourceLocation stateKey)
		{
			return recordChange(stateKey, new MorphStateMachineEntry(Optional.of(new TickTimestamp()), Optional.empty()));
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
	private HashMap<ResourceLocation, MorphStateMachineEntry> states = new HashMap<>();
	
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
	public Optional<MorphStateMachineEntry> query(ResourceLocation stateKey)
	{
		return Optional.ofNullable(states.get(stateKey));
	}
	
	public HashMap<ResourceLocation, MorphStateMachineEntry> getStates()
	{
		return states;
	}
			
	private void setStateEventUnaware(ResourceLocation stateKey, MorphStateMachineEntry stateEntry)
	{
		states.put(stateKey, stateEntry);
	}
		
	public void deserialize(CompoundTag tag)
	{
		clear();
		ListTag stateEntries = tag.getList("states", Tag.TAG_COMPOUND);
		
		for(int i = 0; i < stateEntries.size(); i++)
		{
			CompoundTag stateEntry = stateEntries.getCompound(i);
			
			Optional<TickTimestamp> tickTimestamp = Optional.empty();
			
			if(stateEntry.contains("stateTimeElapsed", Tag.TAG_INT))
			{
				tickTimestamp = Optional.of(new TickTimestamp(stateEntry.getInt("stateTimeElapsed")));
			}
			
			setStateEventUnaware(new ResourceLocation(stateEntry.getString("stateKey")), new MorphStateMachineEntry(tickTimestamp, Optional.ofNullable(stateEntry.getString("stateValue"))));
		}
	}
	
	public CompoundTag serialize()
	{
		CompoundTag tag = new CompoundTag();
		ListTag stateEntries = new ListTag();
		
		tag.put("states", stateEntries);
		
		for(Map.Entry<ResourceLocation, MorphStateMachineEntry> stateEntry : states.entrySet())
		{
			CompoundTag entryTag = new CompoundTag();
			entryTag.putString("stateKey", stateEntry.getKey().toString());
			
			stateEntry.getValue().getValue().ifPresent(value -> entryTag.putString("stateValue", value));
			stateEntry.getValue().getTimeElapsedSinceChange().ifPresent(timeElapsed -> entryTag.putInt("stateTimeElapsed", timeElapsed.getTimeElapsed()));
			
			stateEntries.add(entryTag);
		}
		
		return tag;
	}
	
	// FIXME: !!! RACE CONDITION POSSIBLE !!!
	public void deserializePacket(FriendlyByteBuf buf)
	{
		clear();
		int size = buf.readInt();
		
		for(int i = 0; i < size; i++)
		{
			boolean stateValuePresent = buf.readBoolean();
			boolean stateTimeElapsedSinceChangePresent = buf.readBoolean();
			
			Optional<String> stateValue = Optional.empty();
			Optional<TickTimestamp> stateTimestamp = Optional.empty();
			
			if(stateValuePresent)
			{
				stateValue = Optional.of(buf.readUtf());
			}
			
			if(stateTimeElapsedSinceChangePresent)
			{
				stateTimestamp = Optional.of(new TickTimestamp(buf.readInt()));
			}
			
			states.put(new ResourceLocation(buf.readUtf()), new MorphStateMachineEntry(stateTimestamp, stateValue));
		}
	}
	
	public void serializePacket(FriendlyByteBuf buf)
	{
		buf.writeInt(states.size());
		
		for(Map.Entry<ResourceLocation, MorphStateMachineEntry> stateEntry : states.entrySet())
		{
			buf.writeUtf(stateEntry.getKey().toString());
			buf.writeBoolean(stateEntry.getValue().getValue().isPresent());
			buf.writeBoolean(stateEntry.getValue().getTimeElapsedSinceChange().isPresent());
			
			stateEntry.getValue().getValue().ifPresent(stateValue -> buf.writeUtf(stateValue));
			// Not exact, but approximate; may cause desync if we're not cautious with this
			stateEntry.getValue().getTimeElapsedSinceChange().ifPresent(stateTimeElapsedSinceChange -> buf.writeInt(stateTimeElapsedSinceChange.getTimeElapsed()));
		}
	}
}
