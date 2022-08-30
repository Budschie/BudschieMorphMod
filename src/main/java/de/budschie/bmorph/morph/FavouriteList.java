package de.budschie.bmorph.morph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class FavouriteList
{
	private MorphList morphList;
	
	private HashSet<MorphItem> favourites = new HashSet<>();
	
	public FavouriteList(MorphList morphList)
	{
		this.morphList = morphList;
	}
	
	public CompoundTag serialize()
	{
		CompoundTag nbt = new CompoundTag();
		
		ListTag listTag = new ListTag();
		
		for(MorphItem favouriteItem : favourites)
		{
			listTag.add(NbtUtils.createUUID(favouriteItem.getUUID()));
		}
		
		nbt.put("favouriteMorphs", listTag);
		
		return nbt;
	}
	
	public void deserialize(CompoundTag nbt)
	{
		int[] favouriteIndices = nbt.getIntArray("favouriteIndices");
		
		for(int favourite : favouriteIndices)
		{
			favourites.add(morphList.getMorphArrayList().get(favourite));
		}
		
		ListTag uuidList = nbt.getList("favouriteMorphs", Tag.TAG_INT_ARRAY);
		
		for(int i = 0; i < uuidList.size(); i++)
		{
			Optional<MorphItem> morphItemResult = morphList.getMorphByUUID(NbtUtils.loadUUID(uuidList.get(i)));
			
			if(morphItemResult.isPresent())
			{
				favourites.add(morphItemResult.get());
			}
		}
	}
	
	public void setMorphList(MorphList morphList)
	{
		this.morphList = morphList;
	}
	
	/** Serialized on server **/
	public void serializePacket(FriendlyByteBuf buffer)
	{
		buffer.writeInt(favourites.size());
		
		for(MorphItem morphItem : favourites)
		{
			buffer.writeUUID(morphItem.getUUID());
		}
	}
	
	/** Deserialized on client. **/
	public void deserializePacket(FriendlyByteBuf buffer)
	{		
		favourites.clear();
		
		int size = buffer.readInt();
		
		for(int i = 0; i < size; i++)
		{
			Optional<MorphItem> morphItem = morphList.getMorphByUUID(buffer.readUUID());
			
			if(morphItem.isPresent())
			{
				favourites.add(morphItem.get());
			}
		}
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	/** Deprecated. There will be no replacement for this method, as it just unnecessarily reveals internal state. **/
	public HashSet<FavouriteMorphItem> getFavourites()
	{
		HashSet<FavouriteMorphItem> favouriteMorphItem = new HashSet<>();
		
		// TODO: Remove this bs. This is pure madness. I'll be so happy when I am finally able to remove this.
		for(MorphItem item : favourites)
		{
			Optional<Integer> indexOfItem = morphList.indexOf(item);
			
			if(indexOfItem.isPresent())
			{
				favouriteMorphItem.add(new FavouriteMorphItem(item, indexOfItem.get()));
			}
		}
		
		return favouriteMorphItem;
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	/** Deprecated. Use {@link #addFavourite(UUID)} instead. **/
	public void addFavourite(int indexInMorphList)
	{
		favourites.add(morphList.getMorphArrayList().get(indexInMorphList));
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	/** Deprecated. Use {@link #removeFavourite(UUID)} instead. **/
	public void removeFavourite(int indexInMorphList)
	{
		favourites.remove(morphList.getMorphArrayList().get(indexInMorphList));
	}
	
	public void addFavourite(UUID favourite)
	{
		if(morphList.contains(favourite))
		{
			favourites.add(morphList.getMorphByUUID(favourite).get());
		}
	}
	
	public void removeFavourite(UUID favourite)
	{
		if(morphList.contains(favourite))
		{
			favourites.remove(morphList.getMorphByUUID(favourite).get());
		}
	}
	
	public boolean containsMorphItem(MorphItem morphItem)
	{
		// haha yes funny number
		return favourites.contains(morphItem);
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
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
