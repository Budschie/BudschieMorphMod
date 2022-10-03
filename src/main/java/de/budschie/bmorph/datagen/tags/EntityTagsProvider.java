package de.budschie.bmorph.datagen.tags;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import de.budschie.bmorph.tags.ModEntityTypeTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class EntityTagsProvider extends TagsProvider<EntityType<?>>
{
	private Logger LOGGER = LogManager.getLogger();
	
	public EntityTagsProvider(DataGenerator pGenerator, Registry<EntityType<?>> pRegistry, String modId, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(pGenerator, pRegistry, modId, existingFileHelper);
	}

	@Override
	public String getName()
	{
		return "BMorph Entity Tag Provider";
	}

	@Override
	protected void addTags()
	{
		this.tag(ModEntityTypeTags.DISABLE_SNEAK_TRANSFORM).add(EntityType.WOLF).add(EntityType.CAT).add(EntityType.PARROT);
		this.tag(ModEntityTypeTags.IRON_GOLEM_ALIKE).add(EntityType.IRON_GOLEM);
		this.tag(ModEntityTypeTags.UNDEAD).add(EntityType.ZOGLIN).add(EntityType.PHANTOM).add(EntityType.ZOMBIE).add(EntityType.DROWNED)
		.add(EntityType.HUSK).add(EntityType.ZOMBIE_VILLAGER).add(EntityType.ZOMBIFIED_PIGLIN).add(EntityType.SKELETON).add(EntityType.STRAY).add(EntityType.WITHER_SKELETON);
		
		this.tag(ModEntityTypeTags.PROHIBIT_SPEED_COPY).add(EntityType.VILLAGER);
	}
}
