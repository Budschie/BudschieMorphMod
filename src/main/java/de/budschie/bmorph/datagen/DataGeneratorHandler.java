package de.budschie.bmorph.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.budschie.bmorph.datagen.nbt_handlers.NBTHandlerProvider;
import de.budschie.bmorph.datagen.tags.EntityTagsProvider;
import de.budschie.bmorph.json_integration.JsonMorphNBTHandler;
import de.budschie.bmorph.json_integration.NBTPath;
import de.budschie.bmorph.main.References;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.MOD, modid = References.MODID)
public class DataGeneratorHandler
{
	@SubscribeEvent
	public static void onGatheringData(GatherDataEvent event)
	{
		if(event.includeServer())
		{
			NBTHandlerProvider nbtHandlerProvider = new NBTHandlerProvider(event.getGenerator());
			
			registerNBTHandlerProviders(nbtHandlerProvider);
			
			event.getGenerator().addProvider(nbtHandlerProvider);
			event.getGenerator().addProvider(new EntityTagsProvider(event.getGenerator(), Registry.ENTITY_TYPE, References.MODID, event.getExistingFileHelper()));
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
			provider.addData(entityType, new JsonMorphNBTHandler(new CompoundTag(), new NBTPath[]{new NBTPath("VariantId")}, new ArrayList<>()));
		}
	}
}
