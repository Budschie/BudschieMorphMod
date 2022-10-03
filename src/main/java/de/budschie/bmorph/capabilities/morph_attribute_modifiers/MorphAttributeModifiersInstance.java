package de.budschie.bmorph.capabilities.morph_attribute_modifiers;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstance;
import de.budschie.bmorph.main.References;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MorphAttributeModifiersInstance extends CommonCapabilityInstance<IMorphAttributeModifiers>
{
	public static final Capability<IMorphAttributeModifiers> MORPH_ATTRIBUTE_MODIFIERS_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	public static final ResourceLocation MORPH_ATTRIBUTE_MODIFIERS_CAP_NAME = new ResourceLocation(References.MODID, "morph_attribute_modifiers");

	public MorphAttributeModifiersInstance()
	{
		super(MORPH_ATTRIBUTE_MODIFIERS_CAP_NAME, MORPH_ATTRIBUTE_MODIFIERS_CAP, MorphAttributeModifiers::new);
	}
	
	@SubscribeEvent
	public static void onAttachingCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player)
		{
			event.addCapability(MORPH_ATTRIBUTE_MODIFIERS_CAP_NAME, new MorphAttributeModifiersInstance());
		}
	}
}
