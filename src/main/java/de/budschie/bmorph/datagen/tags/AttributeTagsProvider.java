package de.budschie.bmorph.datagen.tags;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.tags.ModAttributeTags;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.main.EpicFightMod;

public class AttributeTagsProvider extends TagsProvider<Attribute>
{
	private Logger LOGGER = LogManager.getLogger();
	
	public AttributeTagsProvider(PackOutput packOutput, ResourceKey<Registry<Attribute>> registry, CompletableFuture<Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper)
	{
		super(packOutput, registry, lookupProvider, modId, existingFileHelper);
	}

	@Override
	public String getName()
	{
		return "BMorph Entity Tag Provider";
	}

	@Override
	protected void addTags(Provider pProvider)
	{
		TagsProvider.TagAppender<Attribute> blacklistedAttributes = this.tag(ModAttributeTags.ATTRIBUTES_BLACKLISTED_FOR_COPY);
		ForgeRegistries.ATTRIBUTES.forEach(attribute ->
		{
			if(ForgeRegistries.ATTRIBUTES.getKey(attribute).getNamespace().equals(EpicFightMod.MODID))
			{
				blacklistedAttributes.add(keyOfAttribute(attribute));
			}
		});
	}
	
	private static ResourceKey<Attribute> keyOfAttribute(Attribute attribute)
	{
		return ForgeRegistries.ATTRIBUTES.getResourceKey(attribute).get();
	}
}
