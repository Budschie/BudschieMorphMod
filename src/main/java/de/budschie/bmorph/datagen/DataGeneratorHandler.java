package de.budschie.bmorph.datagen;

import java.util.List;
import java.util.stream.Collectors;

import de.budschie.bmorph.datagen.nbt_handlers.NBTHandlerProvider;
import de.budschie.bmorph.json_integration.JsonMorphNBTHandler;
import de.budschie.bmorph.json_integration.NBTPath;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber
public class DataGeneratorHandler
{
	public static void onGatheringData(GatherDataEvent event)
	{
		if(event.includeServer())
		{
			NBTHandlerProvider nbtHandlerProvider = new NBTHandlerProvider();
			
			registerNBTHandlerProviders(nbtHandlerProvider);
			
			event.getGenerator().addProvider(nbtHandlerProvider);
		}
	}
	
	private static void registerNBTHandlerProviders(NBTHandlerProvider provider)
	{
		List<EntityType<?>> entityTypes = ForgeRegistries.ENTITIES.getValues().parallelStream().filter(entityType -> entityType.getRegistryName().getNamespace().equals("betteranimalsplus"))
				.filter(entityType ->
		{
			String entity = entityType.getRegistryName().getPath();
			
			return !entity.equals("golden_goose_egg") && !entity.equals("goose_egg") 
					&& !entity.equals("turkey_egg") && !entity.equals("pheasant_egg");
		}).collect(Collectors.toList());
		
		for(EntityType<?> entityType : entityTypes)
		{
			provider.addData(entityType, new JsonMorphNBTHandler(new CompoundNBT(), new NBTPath("VariantId")));
		}
	}
}
