package de.budschie.bmorph.capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Class that handles serialization and deserialization of data in abilities.
 * There is a transient tag that will be deleted after the morph was changed,
 * and there is also a persistent tag that will stay as long as the ability
 * exists.
 **/
public class AbilitySerializationContext
{
	private HashMap<ResourceLocation, AbilitySerializationObject> abilityMap = new HashMap<>();
	
	public AbilitySerializationObject getOrCreateSerializationObjectForAbility(Ability ability)
	{
		AbilitySerializationObject object = new AbilitySerializationObject();
		abilityMap.put(ability.getResourceLocation(), object);
		
		return object;
	}
	
	public AbilitySerializationObject getSerializationObjectForAbilityOrNull(Ability ability)
	{
		return abilityMap.get(ability.getResourceLocation());
	}
	
	public Optional<AbilitySerializationObject> getSerializationObjectForAbility(Ability ability)
	{
		return Optional.ofNullable(abilityMap.get(ability.getResourceLocation()));
	}
	
	public void deleteSerializationObjectForAbility(Ability ability)
	{
		abilityMap.remove(ability.getResourceLocation());
	}
	
	/** Clears all transient data. **/
	public void clearTransientData()
	{
		abilityMap.forEach((key, value) -> value.setTransientTag(Optional.empty()));
	}
	
	/** Clears transient data for specific ability. **/
	public void clearTransientDataFor(Ability ability)
	{
		AbilitySerializationObject object = abilityMap.get(ability.getResourceLocation());
		
		if(object != null)
			object.setTransientTag(Optional.empty());
	}
	
	/** Collects the serialized data and puts it into a CompoundTag. **/
	public CompoundTag serialize()
	{
		CompoundTag rootTag = new CompoundTag();
		
		for(Map.Entry<ResourceLocation, AbilitySerializationObject> entries : abilityMap.entrySet())
		{
			// Create the tag for the ability to be serialized
			CompoundTag entryTag = new CompoundTag();
			
			// Add the transient and persistent tags to the entry tag
			entries.getValue().getPersistentTag().ifPresent(persistentTag -> entryTag.put("persistent", persistentTag));
			entries.getValue().getTransientTag().ifPresent(transientTag -> entryTag.put("transient", transientTag));
			
			// Add the entry tag to the root tag using the name of the ability as the key
			rootTag.put(entries.getKey().toString(), entryTag);
		}
		
		return rootTag;
	}
	
	public static AbilitySerializationContext deserialize(CompoundTag deserializeFrom)
	{
		AbilitySerializationContext context = new AbilitySerializationContext();
		
		deserializationLoop:
		for(String ability : deserializeFrom.getAllKeys())
		{
			Ability foundAbility = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(new ResourceLocation(ability));
			
			// If the ability doesn't exist anymore, don't bother deserializing it.
			if(foundAbility == null)
				continue deserializationLoop;
			
			AbilitySerializationObject object = new AbilitySerializationObject();
			
			CompoundTag entryTag = deserializeFrom.getCompound(ability);
			
			if(entryTag.contains("transient"))
				object.setTransientTag(Optional.of(entryTag.getCompound("transient")));
			
			if(entryTag.contains("persistent"))
				object.setPersistentTag(Optional.of(entryTag.getCompound("persistent")));
			
			context.abilityMap.put(foundAbility.getResourceLocation(), object);
		}
		
		return context;
	}
	
	public static class AbilitySerializationObject
	{
		private Optional<CompoundTag> transientTag = Optional.empty();
		private Optional<CompoundTag> persistentTag = Optional.empty();
		
		public Optional<CompoundTag> getTransientTag()
		{
			return transientTag;
		}
		
		public Optional<CompoundTag> getPersistentTag()
		{
			return persistentTag;
		}
		
		public void setPersistentTag(Optional<CompoundTag> persistentTag)
		{
			this.persistentTag = persistentTag;
		}
		
		public void setTransientTag(Optional<CompoundTag> transientTag)
		{
			this.transientTag = transientTag;
		}
		
		public CompoundTag createTransientTag()
		{
			this.transientTag = Optional.of(new CompoundTag());
			
			return this.transientTag.get();
		}
		
		public CompoundTag createPersistentTag()
		{
			this.persistentTag = Optional.of(new CompoundTag());
			
			return this.persistentTag.get();
		}
		
		public CompoundTag getOrCreateTransientTag()
		{
			if(transientTag.isEmpty())
				createTransientTag();
			
			return transientTag.get();
		}
		
		public CompoundTag getOrCreatePersistentTag()
		{
			if(persistentTag.isEmpty())
				createPersistentTag();
			
			return persistentTag.get();
		}
	}
}
