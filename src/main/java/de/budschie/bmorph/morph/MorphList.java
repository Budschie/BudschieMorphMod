package de.budschie.bmorph.morph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;

public class MorphList implements Iterable<MorphItem>
{
	private HashMap<EntityType<?>, Integer> entityCount = new HashMap<>();
	
	private HashSet<MorphItem> playerMorphItems = new HashSet<>();
	private HashMap<UUID, MorphItem> uuidToMorphItem = new HashMap<>();
	
	private FavouriteList favouriteList;
	
	public CompoundTag serializeNBT()
	{
		CompoundTag rootTag = new CompoundTag();
		ListTag listTag = new ListTag();
				
		uuidToMorphItem.forEach((key, value) ->
		{
			listTag.add(value.serialize());
		});
		
		rootTag.put("data", listTag);
		
		return rootTag;
	}
	
	private void deserializeNBTOld(CompoundTag tag)
	{
		Set<String> keys = tag.getAllKeys();
		
		for(int i = 0; i < keys.size(); i++)
		{
			CompoundTag morphTag = tag.getCompound(String.valueOf(i));
			
			MorphItem item = MorphHandler.deserializeMorphItem(morphTag);
			playerMorphItems.add(item);
			uuidToMorphItem.put(item.getUUID(), item);
			incrementEntityCount(item.getEntityType());
		}
	}
	
	private void deserializeNBTNew(CompoundTag tag)
	{
		ListTag list = tag.getList("data", Tag.TAG_COMPOUND);
		
		for(int i = 0; i < list.size(); i++)
		{
			CompoundTag morphItemCompound = list.getCompound(i);
			
			MorphItem morphItem = MorphHandler.deserializeMorphItem(morphItemCompound);
			playerMorphItems.add(morphItem);
			uuidToMorphItem.put(morphItem.getUUID(), morphItem);
			incrementEntityCount(morphItem.getEntityType());
		}
	}
	
	public void deserializeNBT(CompoundTag tag)
	{
		if(tag.contains("data", Tag.TAG_LIST))
		{
			deserializeNBTNew(tag);
		}
		else
		{
			deserializeNBTOld(tag);
		}
	}
	
	public void serializePacket(FriendlyByteBuf packet)
	{
		packet.writeInt(uuidToMorphItem.size());
		
		uuidToMorphItem.forEach((uuid, morphItem) ->
		{
			packet.writeNbt(morphItem.serialize());
		});
	}
	
	public void deserializePacket(FriendlyByteBuf packet)
	{		
		int amount = packet.readInt();
		
		playerMorphItems = new HashSet<>(amount);
		uuidToMorphItem = new HashMap<>(amount);
		
		for(int i = 0; i < amount; i++)
		{
			addMorphItem(MorphHandler.deserializeMorphItem(packet.readNbt()));
		}
	}
		
	public void addMorphItem(MorphItem item)
	{
		playerMorphItems.add(item);
		uuidToMorphItem.put(item.getUUID(), item);
		
		incrementEntityCount(item.getEntityType());
	}
			
	public void removeMorphItem(UUID morphItemKey)
	{
		if(uuidToMorphItem.containsKey(morphItemKey))
		{
			MorphItem associatedMorphItem = uuidToMorphItem.get(morphItemKey);
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
