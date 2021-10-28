package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InstaRegenAbility extends AbstractEventAbility
{	
	public static final Codec<InstaRegenAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(ResourceLocation.CODEC.optionalFieldOf("consumed_item_tag").forGetter(InstaRegenAbility::getItemTag),
					Codec.FLOAT.fieldOf("health_regenerated").forGetter(InstaRegenAbility::getHealthRegenerated)).apply(instance, InstaRegenAbility::new));
	
	private Optional<ResourceLocation> itemTag;
	private float healthRegenerated;
	
	/**
	 * An ability that is being passively activated when an item was used.
	 * 
	 * It checks when
	 **/
	public InstaRegenAbility(Optional<ResourceLocation> itemTag, float healthRegenerated)
	{
		this.healthRegenerated = healthRegenerated;
		this.itemTag = itemTag;
	}
	
	public Optional<ResourceLocation> getItemTag()
	{
		return itemTag;
	}
	
	public float getHealthRegenerated()
	{
		return healthRegenerated;
	}
	
	@SubscribeEvent
	public void onEat(LivingEntityUseItemEvent.Finish event)
	{
		if(isTracked(event.getEntity()))
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			if(itemTag.isPresent() ? ItemTags.createOptional(getResourceLocation()).contains(event.getItem().getItem()) : event.getItem().isFood())
			{
				player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healthRegenerated));
			}
		}
	}
}
