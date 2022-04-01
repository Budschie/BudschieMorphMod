package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;

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
public class GuardianBeamCapabilityInstance extends CommonCapabilityInstanceSerializable<IGuardianBeamCapability>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "guardian_beam_cap");
	
	public static final Capability<IGuardianBeamCapability> GUARDIAN_BEAM_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	public GuardianBeamCapabilityInstance(Player player)
	{
		super(CAPABILITY_NAME, GUARDIAN_BEAM_CAP, () -> new GuardianBeamCapability(player));
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player player)
			event.addCapability(CAPABILITY_NAME, new GuardianBeamCapabilityInstance(player));
	}

	@Override
	public void deserializeAdditional(CompoundTag tag, IGuardianBeamCapability instance)
	{
		if(tag.contains("attacked_entity"))
			instance.setAttackedEntityServer(Optional.of(tag.getUUID("attacked_entity")));
		
		instance.setAttackProgression(tag.getInt("attack_progression"));
	}

	@Override
	public void serializeAdditional(CompoundTag tag, IGuardianBeamCapability instance)
	{
		instance.getAttackedEntityServer().ifPresent(uuid -> tag.putUUID("attacked_entity", uuid));
		tag.putInt("attack_progression", instance.getAttackProgression());
	}
}
