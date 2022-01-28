package de.budschie.bmorph.json_integration;

import java.util.Optional;

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
	
	/**
	 * Returns the name of the last node (meaning the final object => the object
	 * this path points to.)
	 **/
	public String getLastNode()
	{
		return nodes[nodes.length - 1];
	}
	
	/** Gets or creates a parent tag for the object represented by the Nbt path. **/
	public CompoundTag getOrCreateParent(CompoundTag compound)
	{
		CompoundTag currentCompound = compound;
		
		// Resolve the path where where we shall copy all nbt data to.
		for(int i = 0; i < nodes.length - 1; i++)
		{
			boolean doesNodeExist = currentCompound.contains(nodes[i]);
			CompoundTag newCompound = currentCompound.getCompound(nodes[i]);
			
			if(!doesNodeExist)
				currentCompound.put(nodes[i], newCompound);
			
			currentCompound = newCompound;
		}
		
		return currentCompound;
	}
	
	public void setTag(CompoundTag root, Tag tag)
	{
		getOrCreateParent(root).put(getLastNode(), tag);
	}
	
	/** Returns the tag that sits at the current nbt path. **/
	public Tag resolve(CompoundTag compoundNBT)
	{
		CompoundTag currentCompound = compoundNBT;
		
		for(int i = 0; i < nodes.length - 1; i++)
			currentCompound = currentCompound.getCompound(nodes[i]);
		
		return currentCompound.get(nodes[nodes.length - 1]);
	}
	
	/** Returns the tag that sits at the current nbt path. **/
	public Optional<Tag> resolveOptional(CompoundTag compoundNBT)
	{
		CompoundTag currentCompound = compoundNBT;
		
		for(int i = 0; i < nodes.length - 1; i++)
			currentCompound = currentCompound.getCompound(nodes[i]);
		
		return currentCompound.contains(getLastNode()) ? Optional.of(currentCompound.get(getLastNode())) : Optional.empty();
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
		
		
		toPath.getOrCreateParent(to).put(toPath.nodes[toPath.nodes.length - 1], fromNBT.copy());
	}
	
	@Override
	public String toString()
	{
		return String.join(";", nodes);
	}
}
