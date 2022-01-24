package de.budschie.bmorph.morph.functionality.codec_addition;

import java.util.List;

import de.budschie.bmorph.json_integration.NBTPath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * The purpose of this class is to represent a modification of existing NBT
 * data. It is an addition to the existing tracking mechanic of NBT data.
 * 
 * An example where you would use this class is when you want to track the Age property
 * of animals. This property can have a visual impact on the entity (thus it should be tracked),
 * but it changes every tick, which could result in you having multiple mobs that look the same (which should really not happen).
 * 
 * Thus I have decided that this class should handle such situations by representing a multitude of operations and if-statements.
 * Every data transformer has a source, a destination and a list of modifiers.
 **/
public class DataTransformer
{
	private NBTPath source;
	private NBTPath destination;
	private List<IDataModifier> modifiers;
	
	public DataTransformer()
	{
		
	}
	
	public void transformData(CompoundTag dataRootSource, CompoundTag dataRootDestination)
	{
		Tag originalTag = source.resolve(dataRootDestination);
		
		for(IDataModifier modifier : modifiers)
		{
			originalTag = modifier.applyModifier(originalTag);
		}
		
		destination.setTag(dataRootDestination, originalTag);
	}
}
