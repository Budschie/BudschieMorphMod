package de.budschie.bmorph.morph;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class BetterAnimalsPlusFallbackMorphData implements Runnable
{
	private static final String VARIANT_KEY_NAME = "VariantId";
	
	@Override
	public void run()
	{
		System.out.println("Initalizing betteranimalsplus support...");
		
		System.out.println(Strings.join(
				ForgeRegistries.ENTITIES.getKeys().stream().filter(registryKey -> registryKey.getNamespace().equals("betteranimalsplus"))
				.map(registryKey -> registryKey.toString())
				.collect(Collectors.toList()),
				';'
				));
		
		// Iterate overy every entity of the betteranimalsplus mod and set its data handler to save the VariantID (this is stupid and temporary, but it will do the trick hopefully).
		// Well, we are luckily doing this fucking mess only once
		List<EntityType<?>> entityTypes = ForgeRegistries.ENTITIES.getValues().parallelStream().filter(entityType -> entityType.getRegistryName().getNamespace().equals("betteranimalsplus")).collect(Collectors.toList());
		
		for(EntityType<?> entityType : entityTypes)
		{
//			MorphManagerHandlers.FALLBACK.addDataHandler(entityType, new SpecialDataHandler((entityA, entityB) -> 
//			{
//				CompoundNBT nbtA = entityA.serialize();
//				CompoundNBT nbtB = entityB.serialize();
//				
//				// This is weird
//				if(nbtA.getString(VARIANT_KEY_NAME) == null)
//				{
//					return nbtB.getString(VARIANT_KEY_NAME) == null;
//				}
//				else if(nbtB.getString(VARIANT_KEY_NAME) == null)
//					return false;
//				
//				return nbtA.getString(VARIANT_KEY_NAME).equals(nbtB.getString(VARIANT_KEY_NAME));
//			}, (type, nbt) -> type.getRegistryName().toString().hashCode() ^ (nbt.getString(VARIANT_KEY_NAME) == null ? 0x57855 : nbt.getString(VARIANT_KEY_NAME).hashCode()), nbt -> 
//			{
//				CompoundNBT newNBT = new CompoundNBT();
//				String variant = nbt.getString(VARIANT_KEY_NAME);
//				
//				if(variant != null)
//					newNBT.putString(VARIANT_KEY_NAME, variant);
//				
//				return newNBT;
//			}));
		}
	}
}
