package de.budschie.bmorph.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

public class MorphList
{
	private HashMap<EntityType<?>, Integer> entityCount = new HashMap<>();
	
	private HashSet<MorphItem> playerMorphItems = new HashSet<>();
	private ArrayList<MorphItem> morphArrayList = new ArrayList<>();
	
	public CompoundTag serializeNBT()
	{
		CompoundTag rootTag = new CompoundTag();
				
		for(int i = 0; i < morphArrayList.size(); i++)
		{
			rootTag.put(Integer.valueOf(i).toString(), morphArrayList.get(i).serialize());
		}
		
		return rootTag;
	}
	
	public void deserializeNBT(CompoundTag tag)
	{
		Set<String> keys = tag.getAllKeys();
		
		for(int i = 0; i < keys.size(); i++)
		{
			CompoundTag morphTag = tag.getCompound(String.valueOf(i));
			
			MorphItem item = MorphHandler.deserializeMorphItem(morphTag);
			playerMorphItems.add(item);
			morphArrayList.add(item);
			incrementEntityCount(item.getEntityType());
		}
	}
	
	public void serializePacket(FriendlyByteBuf packet)
	{
		packet.writeInt(morphArrayList.size());
		
		for(MorphItem item : morphArrayList)
		{
			packet.writeNbt(item.serialize());
		}
	}
	
	public void deserializePacket(FriendlyByteBuf packet)
	{
		int amount = packet.readInt();
		
		morphArrayList = new ArrayList<>(amount);
		
		for(int i = 0; i < amount; i++)
		{
			morphArrayList.add(MorphHandler.deserializeMorphItem(packet.readNbt()));
		}
	}
	
	public int addToMorphList(MorphItem item)
	{
		playerMorphItems.add(item);
		morphArrayList.add(item);
		
		incrementEntityCount(item.getEntityType());
				
		return morphArrayList.size() - 1;
	}
	
	public void removeFromMorphList(int index)
	{
		MorphItem item = morphArrayList.remove(index);
		playerMorphItems.remove(item);
		
		decrementEntityCount(item.getEntityType());
	}
	
	public int getEntityCount(EntityType<?> ofType)
	{
		return entityCount.getOrDefault(ofType, 0);
	}
	
	public boolean contains(MorphItem item)
	{
		return playerMorphItems.contains(item);
	}
	
	public Optional<Integer> indexOf(MorphItem item)
	{
		if(playerMorphItems.contains(item))
		{
			return Optional.of(morphArrayList.indexOf(item));
		}
		
		return Optional.empty();
	}
	
	public ArrayList<MorphItem> getMorphArrayList()
	{
		return morphArrayList;
	}	
	
	private void decrementEntityCount(EntityType<?> type)
	{
		if(entityCount.containsKey(type))
		{
			int currentValue = entityCount.get(type);
			
			if(currentValue <= 1)
				entityCount.remove(type);
			else
				entityCount.put(type, currentValue - 1);
		}
	}
	
	private void incrementEntityCount(EntityType<?> type)
	{
		if(entityCount.containsKey(type))
			entityCount.put(type, entityCount.get(type) + 1);
		else
			entityCount.put(type, 1);
	}
}
