package de.budschie.bmorph.datagen.tags;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import de.budschie.bmorph.tags.ModAttributeTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.main.EpicFightMod;

public class AttributeTagsProvider extends TagsProvider<Attribute>
{
	private Logger LOGGER = LogManager.getLogger();
	
	public AttributeTagsProvider(DataGenerator pGenerator, Registry<Attribute> pRegistry, String modId, @Nullable ExistingFileHelper existingFileHelper)
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
		TagsProvider.TagAppender<Attribute> blacklistedAttributes = this.tag(ModAttributeTags.ATTRIBUTES_BLACKLISTED_FOR_COPY);
		ForgeRegistries.ATTRIBUTES.forEach(attribute ->
		{
			if(attribute.getRegistryName().getNamespace().equals(EpicFightMod.MODID))
			{
				blacklistedAttributes.add(attribute);
			}
		});
	}
}
