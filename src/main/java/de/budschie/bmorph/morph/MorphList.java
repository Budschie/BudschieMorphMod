package de.budschie.bmorph.morph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class MorphList
{
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
		
		return morphArrayList.size() - 1;
	}
	
	public void removeFromMorphList(int index)
	{
		MorphItem item = morphArrayList.remove(index);
		playerMorphItems.remove(item);
	}
	
	public boolean contains(MorphItem item)
	{
		return playerMorphItems.contains(item);
	}
	
	public ArrayList<MorphItem> getMorphArrayList()
	{
		return morphArrayList;
	}	
}
