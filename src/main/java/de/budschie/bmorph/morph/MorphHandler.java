package de.budschie.bmorph.morph;

import java.util.HashMap;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;

public class MorphHandler
{
	private static HashMap<String, Supplier<MorphItem>> morphItems = new HashMap<>();
	
	public static void addMorphItem(String id, Supplier<MorphItem> morphItem)
	{
		morphItems.put(id, morphItem);
	}
	
	public static MorphItem deserializeMorphItem(CompoundTag morphItem)
	{
		Supplier<MorphItem> supplier = morphItems.get(morphItem.getString("id"));
		
		if(supplier == null)
			throw new IllegalArgumentException("The id " + morphItem.getString("id") + " is unknown.");
		else
		{
			MorphItem item = supplier.get();
			item.deserialize(morphItem);
			return item;
		}
	}
}
