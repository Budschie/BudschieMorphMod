package de.budschie.bmorph.capabilities.guardian;

import java.util.Optional;

import de.budschie.bmorph.main.References;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class GuardianBeamCapabilityAttacher implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "guardian_beam_cap");
	
	public static final Capability<IGuardianBeamCapability> GUARDIAN_BEAM_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	private LazyOptional<IGuardianBeamCapability> capability = LazyOptional.of(() -> new GuardianBeamCapability());
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return GUARDIAN_BEAM_CAP.orEmpty(cap, capability);
	}
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new GuardianBeamCapabilityAttacher());
	}

	@Override
	public CompoundTag serializeNBT()
	{
		IGuardianBeamCapability instance = capability.resolve().get();	
		
		CompoundTag nbt = new CompoundTag();
		instance.getAttackedEntityServer().ifPresent(uuid -> nbt.putUUID("attacked_entity", uuid));
		nbt.putInt("attack_progression", instance.getAttackProgression());
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		IGuardianBeamCapability instance = capability.resolve().get();
		
		if(nbt.contains("attacked_entity"))
			instance.setAttackedEntityServer(Optional.of(nbt.getUUID("attacked_entity")));
		
		instance.setAttackProgression(nbt.getInt("attack_progression"));
	}
}
