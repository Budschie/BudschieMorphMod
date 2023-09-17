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
}
