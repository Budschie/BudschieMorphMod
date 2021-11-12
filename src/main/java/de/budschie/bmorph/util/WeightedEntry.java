package de.budschie.bmorph.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class WeightedEntry<T>
{
	private T data;
	private int weight;
	
	public WeightedEntry(int weight, T data)
	{
		if(weight < 0)
			throw new IllegalArgumentException(String.format("The weight may not be smaller then 0, but it is: %s.", weight));
		
		this.data = data;
		this.weight = weight;
	}
	
	public T getData()
	{
		return data;
	}
	
	public int getWeight()
	{
		return weight;
	}
	
	public static <C> Codec<WeightedEntry<C>> codecOf(Codec<C> tCodec)
	{
		return RecordCodecBuilder.create(instance -> 
		instance.group(Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedEntry::getWeight), tCodec.fieldOf("data").forGetter(WeightedEntry::getData))
				.apply(instance, WeightedEntry::new));
	}
}
