package de.budschie.bmorph.capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class MorphStateMachine
{
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
	
	public void setState(String stateKey, String stateValue)
	{
		setStateEventUnaware(stateKey, stateValue);
	}
	
	private void setStateEventUnaware(String stateKey, String stateValue)
	{
		states.put(stateKey, stateValue);
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
