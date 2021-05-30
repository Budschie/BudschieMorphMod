package de.budschie.bmorph.capabilities;

import de.budschie.bmorph.main.References;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MorphCapabilityAttacher implements ICapabilitySerializable<CompoundNBT>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "morph_cap");
	
	@CapabilityInject(IMorphCapability.class)
	public static final Capability<IMorphCapability> MORPH_CAP = null;
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity)
			event.addCapability(CAPABILITY_NAME, new MorphCapabilityAttacher());
	}
	
	public MorphCapabilityAttacher()
	{
		if(MORPH_CAP == null)
			throw new IllegalStateException("Why was the cap not injected?");
	}
		
	LazyOptional<IMorphCapability> cap = LazyOptional.of(MORPH_CAP::getDefaultInstance);
	
	public static void register()
	{
		CapabilityManager.INSTANCE.register(IMorphCapability.class, new MorphCapabilityStorage(), new MorphCapabilityFactory());
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return MORPH_CAP.orEmpty(cap, this.cap);
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		return (CompoundNBT) MORPH_CAP.getStorage().writeNBT(MORPH_CAP, cap.resolve().get(), null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		MORPH_CAP.getStorage().readNBT(MORPH_CAP, cap.resolve().get(), null, nbt);
	}
}
