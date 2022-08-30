package de.budschie.bmorph.morph.functionality.data_transformers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.json_integration.NBTPath;
import de.budschie.bmorph.util.IDynamicRegistryObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

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
public class DataTransformer implements IDynamicRegistryObject
{	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private ResourceLocation name;
	
	private NBTPath source;
	private NBTPath destination;
	private List<DataModifier> modifiers;
	
	public DataTransformer(NBTPath source, NBTPath destination, List<DataModifier> modifiers)
	{
		this.source = source;
		this.destination = destination;
		this.modifiers = modifiers;
	}
	
	public static DataTransformer valueOf(CompoundTag compoundTag)
	{
		ResourceLocation resourceLocation = new ResourceLocation(compoundTag.getString("TransformerId"));
		NBTPath src = NBTPath.valueOf(compoundTag.getString("NbtSrc"));
		NBTPath dest = NBTPath.valueOf(compoundTag.getString("NbtDest"));
		
		ArrayList<DataModifier> modifiers = new ArrayList<>();
		
		int length = compoundTag.getInt("Length");
		
		for(int i = 0; i < length; i++)
		{			
			CompoundTag modifierTag = compoundTag.getCompound(Integer.valueOf(i).toString());
			
			ResourceLocation modifierId = new ResourceLocation(modifierTag.getString("ModifierId"));
			
			DataModifierHolder<?> modifierHolder = DataModifierRegistry.REGISTRY.get().getValue(modifierId);
			
			if(modifierHolder == null)
			{
				LOGGER.error(MessageFormat.format("Skipped data modifier with illegal type id {0} whilst deserializing data transformer {1}. If you see this, please report it as a bug.", modifierId, resourceLocation));
				continue;
			}
			
			Optional<? extends DataModifier> dataModifier = modifierHolder.deserializeNbt(modifierTag.getCompound("Data"));
			
			if(dataModifier.isPresent())
			{
				modifiers.add(dataModifier.get());
			}
			else
			{
				LOGGER.error(MessageFormat.format("Skipped data modifier of type {0} whilst deserializing data transformer {1}. If you see this, please report it as a bug.", modifierHolder.getRegistryName(), resourceLocation));
			}
		}
		
		DataTransformer transformer = new DataTransformer(src, dest, modifiers);
		transformer.setResourceLocation(resourceLocation);
		
		return transformer;
	}
	
	public void transformData(CompoundTag dataRootSource, CompoundTag dataRootDestination)
	{
		Optional<Tag> originalTag = source.resolveOptional(dataRootSource);
		
		for(DataModifier modifier : modifiers)
		{
			if(modifier.canOperateOn(originalTag))
				originalTag = modifier.applyModifier(originalTag);
		}
		
		if(originalTag.isPresent())
			destination.setTag(dataRootDestination, originalTag.get());
	}
	
	public CompoundTag toNbt()
	{
		CompoundTag tag = new CompoundTag();
		
		tag.putString("TransformerId", getResourceLocation().toString());
		
		int i = 0;
		for(DataModifier modifier : modifiers)
		{
			DataModifier currentMod = modifiers.get(i);
			
			Optional<CompoundTag> toSerialize = currentMod.serializeNbt();
			
			if(toSerialize.isPresent())
			{
				CompoundTag modTag = new CompoundTag();
				modTag.putString("ModifierId", modifier.getDataModifierHolder().getRegistryName().toString());
				modTag.put("Data", toSerialize.get());
				
				tag.put(Integer.valueOf(i++).toString(), modTag);
			}
			else
			{
				LOGGER.warn(MessageFormat.format("Skipped data modifier of type {0} in {1} because it could not be serialized. This data can consequently not be used. Skipping this modifier.", modifier.getDataModifierHolder().getRegistryName(), getResourceLocation()));
			}
		}
		
		// Store the length. We don't have to increment i here because we use i++ and not ++i in the upper code.
		tag.putInt("Length", i);
		
		tag.putString("NbtSrc", source.toString());
		tag.putString("NbtDest", destination.toString());
		
		return tag;
	}

	@Override
	public ResourceLocation getResourceLocation()
	{
		return name;
	}

	@Override
	public void setResourceLocation(ResourceLocation name)
	{
		this.name = name;
	}
}
