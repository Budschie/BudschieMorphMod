package de.budschie.bmorph.json_integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class NBTPath
{
	private String[] nodes;
	
	public static final Codec<NBTPath> CODEC = Codec.STRING.comapFlatMap(fromStr -> DataResult.success(NBTPath.valueOf(fromStr)), toStr -> toStr.toString());
	
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
	
	/** Returns the tag that sits at the current nbt path. **/
	public Tag resolve(CompoundTag compoundNBT)
	{
		CompoundTag currentCompound = compoundNBT;
		
		for(int i = 0; i < nodes.length - 1; i++)
			currentCompound = currentCompound.getCompound(nodes[i]);
		
		return currentCompound.get(nodes[nodes.length - 1]);
	}
	
	public void copyTo(CompoundTag from, CompoundTag to)
	{
		copyTo(from, to, this);
	}
	
	/**
	 * Copies the element sitting at this nbt path to the given compound tag and the
	 * given toPath.
	 * 
	 * @param from   This is the tag from which we shall copy the data from.
	 * @param to     This is the tag to which we shall copy the data to.
	 * @param toPath This is the nbt path representing the desination to which we
	 *               shall copy.
	 **/
	public void copyTo(CompoundTag from, CompoundTag to, NBTPath toPath)
	{
		Tag fromNBT = resolve(from);
		
		if(fromNBT == null)
			return;
		
		CompoundTag currentCompound = to;
		
		// Resolve the path where where we shall copy all nbt data to.
		for(int i = 0; i < toPath.nodes.length - 1; i++)
		{
			CompoundTag newCompound = currentCompound.getCompound(toPath.nodes[i]);
			currentCompound.put(toPath.nodes[i], newCompound);
			currentCompound = newCompound;
		}
		
		currentCompound.put(toPath.nodes[toPath.nodes.length - 1], fromNBT.copy());
	}
	
	@Override
	public String toString()
	{
		return String.join(";", nodes);
	}
}
