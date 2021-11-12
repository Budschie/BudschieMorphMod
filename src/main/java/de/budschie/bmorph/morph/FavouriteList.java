package de.budschie.bmorph.morph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class FavouriteList
{
	private MorphList morphList;
	
	private HashSet<FavouriteMorphItem> favourites = new HashSet<>();
	
	public FavouriteList(MorphList morphList)
	{
		this.morphList = morphList;
	}
	
	public CompoundTag serialize()
	{
		CompoundTag nbt = new CompoundTag();
		
		nbt.putIntArray("favouriteIndices", favourites.stream().map(item -> item.morphListIndex).collect(Collectors.toList()));
		
		return nbt;
	}
	
	public void deserialize(CompoundTag nbt)
	{
		int[] favouriteIndices = nbt.getIntArray("favouriteIndices");
		
		for(int favourite : favouriteIndices)
			favourites.add(new FavouriteMorphItem(morphList.getMorphArrayList().get(favourite), favourite));
	}
	
	public void setMorphList(MorphList morphList)
	{
		this.morphList = morphList;
	}
	
	/** Serialized on server **/
	public void serializePacket(FriendlyByteBuf buffer)
	{
		int[] favouriteIndices = new int[favourites.size()];
		int i = 0;
		
		Iterator<FavouriteMorphItem> items = favourites.iterator();
		
		while(items.hasNext())
			favouriteIndices[i++] = items.next().morphListIndex;
		
		buffer.writeVarIntArray(favouriteIndices);
	}
	
	/** Deserialized on client. **/
	public void deserializePacket(FriendlyByteBuf buffer)
	{
		int[] favouriteIndices = buffer.readVarIntArray();
		
		favourites.clear();
		
		for(int favouriteIndex : favouriteIndices)
			addFavourite(favouriteIndex);
	}
	
	public HashSet<FavouriteMorphItem> getFavourites()
	{
		return favourites;
	}
	
	public void addFavourite(int indexInMorphList)
	{
		favourites.add(new FavouriteMorphItem(morphList.getMorphArrayList().get(indexInMorphList), indexInMorphList));
	}
	
	public void removeFavourite(int indexInMorphList)
	{
		favourites.remove(new FavouriteMorphItem(morphList.getMorphArrayList().get(indexInMorphList), indexInMorphList));
	}
	
	public boolean containsMorphItem(MorphItem morphItem)
	{
		// haha yes funny number
		return favourites.contains(new FavouriteMorphItem(morphItem, 696969420));
	}
	
	public static class FavouriteMorphItem
	{
		private MorphItem morphItem;
		private int morphListIndex;
		
		public FavouriteMorphItem(MorphItem morphItem, int morphListIndex)
		{
			this.morphItem = morphItem;
			this.morphListIndex = morphListIndex;
		}
		
		public MorphItem getMorphItem()
		{
			return morphItem;
		}
		
		public int getMorphListIndex()
		{
			return morphListIndex;
		}
		
		@Override
		public int hashCode()
		{
			return morphItem.hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof FavouriteMorphItem && ((FavouriteMorphItem)obj).morphItem.equals(this.morphItem);
		}
	}
}
