package de.budschie.bmorph.morph.functionality.data_transformers;

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.registries.ForgeRegistryEntry;

/** This class is a singleton holding a codec and a name for IDataModifiers. **/
public class DataModifierHolder<T extends DataModifier> extends ForgeRegistryEntry<DataModifierHolder<? extends DataModifier>>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Codec<T> dataModifierCodec;
	
	public DataModifierHolder(Codec<T> dataModifierCodec)
	{
		this.dataModifierCodec = dataModifierCodec;
	}
	
	public Codec<T> getCodec()
	{
		return dataModifierCodec;
	}
	
	public Optional<? extends DataModifier> deserializeJson(JsonElement object)
	{
		Optional<Pair<T, JsonElement>> optAbility = this.dataModifierCodec.decode(JsonOps.INSTANCE, object).resultOrPartial(err -> LOGGER.warn("Received bad data when parsing data modifier: " + err));
		
		if(optAbility.isPresent())
		{
			optAbility.get().getFirst().setDataModifierHolder(this);
			return Optional.of(optAbility.get().getFirst());
		}
		else
		{
			return Optional.empty();
		}
	}
	
	public Optional<? extends DataModifier> deserializeNbt(CompoundTag nbt)
	{
		Optional<Pair<T, Tag>> optAbility = this.dataModifierCodec.decode(NbtOps.INSTANCE, nbt).resultOrPartial(err -> LOGGER.warn("Received bad data when parsing data modifier: " + err));

		if(optAbility.isPresent())
		{
			optAbility.get().getFirst().setDataModifierHolder(this);
			return Optional.of(optAbility.get().getFirst());
		}
		else
		{
			return Optional.empty();
		}
	}
	
	public Optional<CompoundTag> serializeNBT(T modifier)
	{
		DataResult<Tag> nbt = getCodec().encodeStart(NbtOps.INSTANCE, modifier);
		
		if(nbt.get().left().isPresent())
		{
			if(nbt.get().left().get() instanceof CompoundTag)
				return Optional.of((CompoundTag)nbt.get().left().get());
			else
				return Optional.of(new CompoundTag());
		}
		else
			LOGGER.warn(MessageFormat.format("There was an error serializing a data modifier with its codec {0}: {1}", this.getRegistryName(), nbt.get().right().get().message()));
		
		return Optional.empty();
	}

	// Fcking copy pasta
	@SuppressWarnings("unchecked")
	public Optional<CompoundTag> serializeNBTIAmTooDumbForJava(DataModifier modifier)
	{
		if(modifier.getDataModifierHolder() == this)
			return serializeNBT((T) modifier);
		else
			throw new UnsupportedOperationException("You can only call this on the data modifier that belongs to you. If you see this error message, please consider filing a bug report at my GitHub page.");
	}
}
