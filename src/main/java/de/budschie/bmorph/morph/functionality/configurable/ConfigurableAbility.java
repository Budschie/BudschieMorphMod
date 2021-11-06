package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ConfigurableAbility<A extends Ability> extends ForgeRegistryEntry<ConfigurableAbility<A>>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	private Codec<A> codec;
	
	public ConfigurableAbility(Codec<A> codec)
	{
		this.codec = codec;
	}
	
	public Codec<A> getCodec()
	{
		return codec;
	}
	
	public Optional<Ability> deserialize(JsonElement object)
	{
		Optional<Pair<A, JsonElement>> optAbility = this.codec.decode(JsonOps.INSTANCE, object).resultOrPartial(err -> LOGGER.warn("Received bad data when parsing configurable ability: " + err));
		optAbility.ifPresent(p -> p.getFirst().setConfigurableAbility(this));
		return optAbility.isPresent() ? Optional.of(optAbility.get().getFirst()) : Optional.empty();
	}
	
	public Optional<Ability> deserializeNBT(CompoundNBT nbt)
	{
		Optional<Pair<A, INBT>> optAbility = this.codec.decode(NBTDynamicOps.INSTANCE, nbt).resultOrPartial(err -> LOGGER.warn("Could not parse ability data from NBT: " + err));
		optAbility.ifPresent(p -> p.getFirst().setConfigurableAbility(this));
		return optAbility.isPresent() ? Optional.of(optAbility.get().getFirst()) : Optional.empty();
	}
	
	public Optional<CompoundNBT> serializeNBT(A ability)
	{
		DataResult<INBT> nbt = getCodec().encodeStart(NBTDynamicOps.INSTANCE, ability);
		
		if(nbt.get().left().isPresent())
		{
			if(nbt.get().left().get() instanceof CompoundNBT)
				return Optional.of((CompoundNBT)nbt.get().left().get());
			else
				return Optional.of(new CompoundNBT());
		}
		else
			LOGGER.warn("There was an error serializing the ability %s with its codec %s: %s", ability.getResourceLocation(), this.getRegistryName(), nbt.get().right().get().message());
		
		return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	public Optional<CompoundNBT> serializeNBTIAmTooDumbForJava(Ability ability)
	{
		if(ability.getConfigurableAbility() == this)
			return serializeNBT((A) ability);
		else
			return Optional.empty();
	}
	
	@Override
	public int hashCode()
	{
		return this.getRegistryName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof ConfigurableAbility)
		{
			ConfigurableAbility<?> otherConfigAbility = (ConfigurableAbility<?>) obj;
			
			if(otherConfigAbility.getRegistryName().equals(this.getRegistryName()))
				return true;
		}
		
		return false;
	}
	
	public static class SerializationResult
	{
		
	}
}
