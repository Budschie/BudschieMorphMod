package de.budschie.bmorph.morph.functionality.data_transformers;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.nbt.Tag;

public class SetIfPresentModifier extends DataModifier
{
	public static final Codec<SetIfPresentModifier> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(ModCodecs.NBT_TAG.fieldOf("if_present").forGetter(SetIfPresentModifier::getOutputIfPresent)).apply(instance, SetIfPresentModifier::new));
	
	private Tag outputIfPresent;
	
	public SetIfPresentModifier(Tag outputIfPresent)
	{
		this.outputIfPresent = outputIfPresent;
	}
	
	@Override
	public boolean canOperateOn(Optional<Tag> nbtTag)
	{
		return nbtTag.isPresent();
	}

	@Override
	public Optional<Tag> applyModifier(Optional<Tag> inputTag)
	{
		return Optional.of(outputIfPresent.copy());
	}
	
	public Tag getOutputIfPresent()
	{
		return outputIfPresent;
	}
}
