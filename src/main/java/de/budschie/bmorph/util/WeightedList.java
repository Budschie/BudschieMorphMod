package de.budschie.bmorph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class WeightedList<T>
{
	private List<WeightedEntry<T>> weights;
	private int accumulated;
	
	public WeightedList(List<WeightedEntry<T>> entries)
	{
		// Copy list
		this.weights = new ArrayList<>();
		entries.forEach(weight -> 
		{
			accumulated += weight.getWeight();
			this.weights.add(weight);
		});
		
		// Sort from biggest (index 0) to smallest (index n) weight to optimize.
		this.weights.sort((o1, o2) -> Integer.compare(o2.getWeight(), o1.getWeight()));
	}
	
	public T getRandom(Random rand)
	{
		int toAchieve = rand.nextInt(accumulated);
		
		int currentB = 0;
		
		for(int i = 0; i < weights.size(); i++)
		{
			WeightedEntry<T> currentWeight = weights.get(i);			
			currentB += currentWeight.getWeight();
			
			if(currentB > toAchieve)
				return currentWeight.getData();
		}
		
		throw new IllegalArgumentException("Alright this should not happen. There was an error getting the random thingy to work. Please report this to the mod author immediately.");
	}
	
	public static <T> Codec<WeightedList<T>> codecOf(Codec<T> codec)
	{
		return WeightedEntry.codecOf(codec).listOf().flatXmap(convertToList -> DataResult.success(new WeightedList<>(convertToList)),
				convertFrom -> DataResult.success(convertFrom.weights));
	}
}
