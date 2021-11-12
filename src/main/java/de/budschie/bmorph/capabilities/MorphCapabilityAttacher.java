package de.budschie.bmorph.capabilities;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphHandler;
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

@EventBusSubscriber
public class MorphCapabilityAttacher implements ICapabilitySerializable<CompoundTag>
{
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "morph_cap");
	
	public static final Capability<IMorphCapability> MORPH_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new MorphCapabilityAttacher());
	}
	
	public MorphCapabilityAttacher()
	{
		
	}
		
	LazyOptional<IMorphCapability> cap = LazyOptional.of(() -> new DefaultMorphCapability());
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
	{
		return MORPH_CAP.orEmpty(cap, this.cap);
	}

	@Override
	public CompoundTag serializeNBT()
	{
		CompoundTag capTag = new CompoundTag();
		
		IMorphCapability instance = cap.resolve().get();
		
		if(instance.getCurrentMorphItem().isPresent())
			capTag.put("currentMorphItem", instance.getCurrentMorphItem().get().serialize());
		
		if(instance.getCurrentMorphIndex().isPresent())
			capTag.putInt("currentMorphIndex", instance.getCurrentMorphIndex().get());
		
		capTag.put("morphList", instance.getMorphList().serializeNBT());
		
		capTag.put("favouriteList", instance.getFavouriteList().serialize());
		
		capTag.putInt("aggroDuration", Math.max(0, instance.getLastAggroDuration() - (ServerSetup.server.getTickCount() - instance.getLastAggroTimestamp())));
		
		return capTag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		CompoundTag capTag = nbt;
		
		IMorphCapability instance = cap.resolve().get();
		
		boolean hasItem = capTag.contains("currentMorphItem");
		boolean hasIndex = capTag.contains("currentMorphIndex");
		
		if(hasItem)
		{
			instance.setMorph(MorphHandler.deserializeMorphItem(capTag.getCompound("currentMorphItem")));
		}
		else if(hasIndex)
		{
			instance.setMorph(capTag.getInt("currentMorphIndex"));
		}
		
		instance.getMorphList().deserializeNBT(capTag.getCompound("morphList"));
		
		instance.getFavouriteList().deserialize(capTag.getCompound("favouriteList"));
		
		instance.setLastAggroDuration(capTag.getInt("aggroDuration"));
	}
}
