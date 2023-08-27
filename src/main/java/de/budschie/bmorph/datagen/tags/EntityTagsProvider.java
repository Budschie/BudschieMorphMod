package de.budschie.bmorph.datagen.tags;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.tags.ModEntityTypeTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTagsProvider extends TagsProvider<EntityType<?>>
{
	private Logger LOGGER = LogManager.getLogger();

	public EntityTagsProvider(PackOutput packOutput, ResourceKey<Registry<EntityType<?>>> registry, CompletableFuture<Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper)
	{
		super(packOutput, registry, lookupProvider, modId, existingFileHelper);
	}
	
	@Override
	public String getName()
	{
		return "BMorph Entity Tag Provider";
	}

	private static <T extends Entity> ResourceKey<EntityType<?>> keyOfEntity(EntityType<T> entityType)
	{
		return ForgeRegistries.ENTITY_TYPES.getResourceKey(entityType).get();
	}
	
	@Override
	protected void addTags(Provider pProvider)
	{
		this.tag(ModEntityTypeTags.DISABLE_SNEAK_TRANSFORM).add(keyOfEntity(EntityType.WOLF)).add(keyOfEntity(EntityType.CAT)).add(keyOfEntity(EntityType.PARROT));
		this.tag(ModEntityTypeTags.IRON_GOLEM_ALIKE).add(keyOfEntity(EntityType.IRON_GOLEM));
		this.tag(ModEntityTypeTags.UNDEAD).add(keyOfEntity(EntityType.ZOGLIN)).add(keyOfEntity(EntityType.PHANTOM)).add(keyOfEntity(EntityType.ZOMBIE)).add(keyOfEntity(EntityType.DROWNED))
		.add(keyOfEntity(EntityType.HUSK)).add(keyOfEntity(EntityType.ZOMBIE_VILLAGER)).add(keyOfEntity(EntityType.ZOMBIFIED_PIGLIN)).add(keyOfEntity(EntityType.SKELETON)).add(keyOfEntity(EntityType.STRAY)).add(keyOfEntity(EntityType.WITHER_SKELETON));
		
		this.tag(ModEntityTypeTags.PROHIBIT_SPEED_COPY).add(keyOfEntity(EntityType.VILLAGER));
	}
}
