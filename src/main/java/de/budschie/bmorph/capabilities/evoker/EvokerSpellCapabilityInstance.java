package de.budschie.bmorph.capabilities.evoker;

import de.budschie.bmorph.capabilities.common.CommonCapabilityInstanceSerializable;
import de.budschie.bmorph.main.References;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class EvokerSpellCapabilityInstance extends CommonCapabilityInstanceSerializable<IEvokerSpellCapability>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "evoker_spell_cap");
	public static final Capability<IEvokerSpellCapability> EVOKER_SPELL_CAP = CapabilityManager.get(new CapabilityToken<>(){});

	public EvokerSpellCapabilityInstance()
	{
		super(CAPABILITY_NAME, EVOKER_SPELL_CAP, EvokerSpellCapability::new);
	}

	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player)
		{
			event.addCapability(CAPABILITY_NAME, new EvokerSpellCapabilityInstance());
		}
	}
	
	@Override
	public void deserializeAdditional(CompoundTag tag, IEvokerSpellCapability instance)
	{
		instance.setCastingTicks(tag.getInt("casting_ticks"));
		instance.setFangsTimePoint(tag.getInt("fangs_time_point"));
		instance.setRange(tag.getDouble("range"));
	}

	@Override
	public void serializeAdditional(CompoundTag tag, IEvokerSpellCapability instance)
	{
		tag.putInt("casting_ticks", instance.getCastingTicksLeft());
		tag.putInt("fangs_time_point", instance.getFangsTimePoint());
		tag.putDouble("range", instance.getRange());
	}
}
