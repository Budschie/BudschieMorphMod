package de.budschie.bmorph.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.budschie.bmorph.datagen.nbt_handlers.NBTHandlerProvider;
import de.budschie.bmorph.datagen.tags.AttributeTagsProvider;
import de.budschie.bmorph.datagen.tags.EntityTagsProvider;
import de.budschie.bmorph.json_integration.JsonMorphNBTHandler;
import de.budschie.bmorph.json_integration.NBTPath;
import de.budschie.bmorph.main.References;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.MOD, modid = References.MODID)
public class DataGeneratorHandler
{
	@SubscribeEvent
	public static void onGatheringData(GatherDataEvent event)
	{
		if(event.includeServer())
		{
			NBTHandlerProvider nbtHandlerProvider = new NBTHandlerProvider(event.getGenerator().getPackOutput());
			
			registerNBTHandlerProviders(nbtHandlerProvider);
			
			event.getGenerator().addProvider(true, nbtHandlerProvider);
			event.getGenerator().addProvider(true, new EntityTagsProvider(event.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, event.getLookupProvider(), References.MODID, event.getExistingFileHelper()));
//			event.getGenerator().addProvider(true, new AttributeTagsProvider(event.getGenerator().getPackOutput(), Registries.ATTRIBUTE, event.getLookupProvider(), References.MODID, event.getExistingFileHelper()));
		}
	}
	
	private static void registerNBTHandlerProviders(NBTHandlerProvider provider)
	{		
		List<EntityType<?>> entityTypes = ForgeRegistries.ENTITY_TYPES.getValues().parallelStream().filter(entityType -> ForgeRegistries.ENTITY_TYPES.getKey(entityType).getNamespace().equals("betteranimalsplus"))
				.filter(entityType ->
		{
			String entity = ForgeRegistries.ENTITY_TYPES.getKey(entityType).getPath();
			
			return !entity.equals("golden_goose_egg") && !entity.equals("goose_egg") 
					&& !entity.equals("turkey_egg") && !entity.equals("pheasant_egg");
		}).collect(Collectors.toList());
		
		for(EntityType<?> entityType : entityTypes)
		{
			provider.addData(entityType, new JsonMorphNBTHandler(new CompoundTag(), new NBTPath[]{new NBTPath("VariantId")}, new ArrayList<>()));
		}
	}
}
