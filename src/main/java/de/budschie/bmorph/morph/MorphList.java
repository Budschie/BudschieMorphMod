package de.budschie.bmorph.morph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;

public class MorphList
{
	private HashSet<MorphItem> playerMorphItems = new HashSet<>();
	
	public CompoundNBT serialize()
	{
		CompoundNBT tag = new CompoundNBT();
		
		Iterator<MorphItem> it = playerMorphItems.iterator();
		
		for(int i = 0; i < playerMorphItems.size(); i++)
		{
			tag.put(Integer.valueOf(i).toString(), it.next().serialize());
		}
		
		return tag;
	}
	
	public void deserialize(CompoundNBT tag)
	{
		Set<String> keys = tag.keySet();
		
		for(String str : keys)
		{
			CompoundNBT morphTag = tag.getCompound(str);
			
			playerMorphItems.add(MorphHandler.deserializeMorphItem(morphTag));
		}
	}
	
	public void addToMorphList(MorphItem item)
	{
		playerMorphItems.add(item);
	}
	
	public void removeFromMorphItem(MorphItem item)
	{
		playerMorphItems.remove(item);
	}
	
	public boolean contains(MorphItem item)
	{
		return playerMorphItems.contains(item);
	}
}
