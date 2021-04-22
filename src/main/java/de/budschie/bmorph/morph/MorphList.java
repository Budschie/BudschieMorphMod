package de.budschie.bmorph.morph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class MorphList
{
	private HashSet<MorphItem> playerMorphItems = new HashSet<>();
	private ArrayList<MorphItem> morphArrayList = new ArrayList<>();
	
	public CompoundNBT serializeNBT()
	{
		CompoundNBT tag = new CompoundNBT();
		
		Iterator<MorphItem> it = playerMorphItems.iterator();
		
		for(int i = 0; i < playerMorphItems.size(); i++)
		{
			tag.put(Integer.valueOf(i).toString(), it.next().serialize());
		}
		
		return tag;
	}
	
	public void deserializeNBT(CompoundNBT tag)
	{
		Set<String> keys = tag.keySet();
		
		for(String str : keys)
		{
			CompoundNBT morphTag = tag.getCompound(str);
			
			MorphItem item = MorphHandler.deserializeMorphItem(morphTag);
			playerMorphItems.add(item);
			morphArrayList.add(item);
		}
	}
	
	public void serializePacket(PacketBuffer packet)
	{
		packet.writeInt(morphArrayList.size());
		
		for(MorphItem item : morphArrayList)
		{
			packet.writeCompoundTag(item.serialize());
		}
	}
	
	public void deserializePacket(PacketBuffer packet)
	{
		int amount = packet.readInt();
		
		morphArrayList = new ArrayList<>(amount);
		
		for(int i = 0; i < amount; i++)
		{
			morphArrayList.add(MorphHandler.deserializeMorphItem(packet.readCompoundTag()));
		}
	}
	
	public void addToMorphList(MorphItem item)
	{
		playerMorphItems.add(item);
		morphArrayList.add(item);
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
