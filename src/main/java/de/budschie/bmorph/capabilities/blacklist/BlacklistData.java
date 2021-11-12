package de.budschie.bmorph.capabilities.blacklist;

import java.util.HashSet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class BlacklistData extends WorldConfigHandler
{
	public BlacklistData()
	{
		super("bmorph_blacklist.dat");
	}

	private HashSet<ResourceLocation> blacklistEntries = new HashSet<>();

	public void addBlacklist(ResourceLocation rs)
	{
		blacklistEntries.add(rs);
	}

	public void removeBlacklist(ResourceLocation rs)
	{
		blacklistEntries.remove(rs);
	}

	public boolean isInBlacklist(ResourceLocation rs)
	{
		return blacklistEntries.contains(rs);
	}

	@SuppressWarnings("unchecked")
	public HashSet<ResourceLocation> getBlacklist()
	{
		return (HashSet<ResourceLocation>) blacklistEntries.clone();
	}

	@Override
	public CompoundTag write()
	{
		CompoundTag tag = new CompoundTag();
		
		HashSet<ResourceLocation> entries = getBlacklist();
		
		int i = 0;
		for(ResourceLocation entry : entries)
		{
			tag.putString(Integer.valueOf(i++).toString(), entry.toString());
		}
		
		tag.putInt("size", i);
		
		return tag;
	}

	@Override
	public void read(CompoundTag tag)
	{		
		int size = tag.getInt("size");
		
		for(int i = 0; i < size; i++)
			addBlacklist(new ResourceLocation(tag.getString(Integer.valueOf(i).toString())));
	}

}
