package de.budschie.bmorph.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

public class MorphList implements Iterable<MorphItem>
{
	private HashMap<EntityType<?>, Integer> entityCount = new HashMap<>();
	
	private HashSet<MorphItem> playerMorphItems = new HashSet<>();
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	private ArrayList<MorphItem> morphArrayList = new ArrayList<>();
	private HashMap<UUID, MorphItem> uuidToMorphItem = new HashMap<>();
	
	private FavouriteList favouriteList;
	
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
			uuidToMorphItem.put(item.getUUID(), item);
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
		playerMorphItems = new HashSet<>(amount);
		uuidToMorphItem = new HashMap<>(amount);
		
		for(int i = 0; i < amount; i++)
		{
			addToMorphList(MorphHandler.deserializeMorphItem(packet.readNbt()));
		}
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public int addToMorphList(MorphItem item)
	{
		playerMorphItems.add(item);
		morphArrayList.add(item);
		uuidToMorphItem.put(item.getUUID(), item);
		
		incrementEntityCount(item.getEntityType());
				
		return morphArrayList.size() - 1;
	}
	
	public void addMorphItem(MorphItem item)
	{
//		playerMorphItems.add(item);
//		morphArrayList.add(item);
//		uuidToMorphItem.put(item.getUUID(), item);
//		
//		incrementEntityCount(item.getEntityType());
//				
//		return morphArrayList.size() - 1;
		
		addToMorphList(item);
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public void removeFromMorphList(int index)
	{
		MorphItem item = morphArrayList.remove(index);
		playerMorphItems.remove(item);
		uuidToMorphItem.remove(item.getUUID());
		
		favouriteList.removeFavourite(item.getUUID());
		
		decrementEntityCount(item.getEntityType());
	}
	
	// Returns the index of the given morph item. Deprecated, DO NOT USE THIS.
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public Optional<Integer> indexOf(MorphItem morphItem)
	{
		for(int i = 0; i < morphArrayList.size(); i++)
		{
			if(morphArrayList.get(i).equals(morphItem))
			{
				return Optional.of(i);
			}
		}
		
		return Optional.empty();
	}
	
	public void removeMorphItem(UUID morphItemKey)
	{
		if(uuidToMorphItem.containsKey(morphItemKey))
		{
			MorphItem associatedMorphItem = uuidToMorphItem.get(morphItemKey);
			morphArrayList.remove(associatedMorphItem);
			playerMorphItems.remove(associatedMorphItem);
			uuidToMorphItem.remove(morphItemKey);
			
			favouriteList.removeFavourite(morphItemKey);
			
			decrementEntityCount(associatedMorphItem.getEntityType());
		}
	}
	
	public Optional<MorphItem> getMorphByUUID(UUID uuid)
	{
		return Optional.ofNullable(uuidToMorphItem.get(uuid));
	}
	
	public int getEntityCount(EntityType<?> ofType)
	{
		return entityCount.getOrDefault(ofType, 0);
	}
	
	public boolean contains(MorphItem item)
	{
		return playerMorphItems.contains(item);
	}
	
	public boolean contains(UUID uuid)
	{
		return uuidToMorphItem.containsKey(uuid);
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public ArrayList<MorphItem> getMorphArrayList()
	{
		return morphArrayList;
	}
	
	public void setFavouriteList(FavouriteList favouriteList)
	{
		this.favouriteList = favouriteList;
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

	@Override
	public Iterator<MorphItem> iterator()
	{
		return playerMorphItems.iterator();
	}
}
