package de.budschie.bmorph.attributes;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD)
public class AttributeAddedEvent
{
	@SubscribeEvent
	public static void onAttributesCreated(EntityAttributeModificationEvent event)
	{
		event.add(EntityType.PLAYER, BMorphAttributes.ATTACK_RANGE.get());
	}
}
