package de.budschie.bmorph.json_integration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NBTPath
{
	private String[] nodes;
	
	public static NBTPath valueOf(String path)
	{
		return new NBTPath(path.split(";"));
	}
	
	public NBTPath(String...nodes)
	{
		this.nodes = nodes;
	}
	
	public String[] getNodes()
	{
		return nodes;
	}
	
	public Tag resolve(CompoundTag compoundNBT)
	{
		CompoundTag currentCompound = compoundNBT;
		
		for(int i = 0; i < nodes.length - 1; i++)
			currentCompound = currentCompound.getCompound(nodes[i]);
		
		return currentCompound.get(nodes[nodes.length - 1]);
	}
	
	public void copyTo(CompoundTag from, CompoundTag to)
	{
		Tag fromNBT = resolve(from);
		
		if(fromNBT == null)
			return;
		
		CompoundTag currentCompound = to;
		
		for(int i = 0; i < nodes.length - 1; i++)
		{
			CompoundTag newCompound = currentCompound.getCompound(nodes[i]);
			currentCompound.put(nodes[i], newCompound);
			currentCompound = newCompound;
		}
		
		currentCompound.put(nodes[nodes.length - 1], fromNBT.copy());
	}
	
	@Override
	public String toString()
	{
		return String.join(";", nodes);
	}
}
