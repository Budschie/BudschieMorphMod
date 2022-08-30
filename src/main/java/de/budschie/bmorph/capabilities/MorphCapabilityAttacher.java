package de.budschie.bmorph.capabilities;

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
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
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final ResourceLocation CAPABILITY_NAME = new ResourceLocation(References.MODID, "morph_cap");
	
	public static final Capability<IMorphCapability> MORPH_CAP = CapabilityManager.get(new CapabilityToken<>(){});
	
	@SubscribeEvent
	public static void onAttachCapsOnPlayer(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof Player)
			event.addCapability(CAPABILITY_NAME, new MorphCapabilityAttacher((Player) event.getObject()));
	}
	
	private Player owner;
	
	public MorphCapabilityAttacher(Player owner)
	{
		this.owner = owner;
	}
		
	LazyOptional<IMorphCapability> cap = LazyOptional.of(() -> new DefaultMorphCapability(owner));
	
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
		
		if(instance.getCurrentMorph().isPresent())
		{			
			capTag.put("currentMorphItem", instance.getCurrentMorph().get().serialize());
			capTag.putString("morphReason", instance.getMorphReason().getRegistryName().toString());
		}
		
		capTag.put("morphList", instance.getMorphList().serializeNBT());
		
		capTag.put("favouriteList", instance.getFavouriteList().serialize());
		
		capTag.putInt("aggroDuration", Math.max(0, instance.getLastAggroDuration() - (owner.getLevel().getServer().getTickCount() - instance.getLastAggroTimestamp())));
		
		capTag.put("savableAbilities", instance.serializeSavableAbilityData());
		
		return capTag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		CompoundTag capTag = nbt;
		
		IMorphCapability instance = cap.resolve().get();
		
		instance.getMorphList().deserializeNBT(capTag.getCompound("morphList"));
		
		boolean hasItem = capTag.contains("currentMorphItem");
		boolean hasIndex = capTag.contains("currentMorphIndex");
		String morphReasonString = capTag.getString("morphReason");
		MorphReason morphReason = morphReasonString == null ? MorphReasonRegistry.NONE.get() : (MorphReasonRegistry.REGISTRY.get().getValue(new ResourceLocation(morphReasonString)));		
		
		if(morphReason == null)
		{
			morphReason = MorphReasonRegistry.NONE.get();
		}
		
		try
		{
			if(hasItem)
			{
				instance.setMorph(MorphHandler.deserializeMorphItem(capTag.getCompound("currentMorphItem")), morphReason);
			}
			if(hasIndex)
			{
				instance.setMorph(instance.getMorphList().getMorphArrayList().get(capTag.getInt("currentMorphIndex")), morphReason);
			}
			
			if(!(hasIndex || hasItem))
			{
				instance.demorph(morphReason);
			}
		}
		catch(IllegalArgumentException | IndexOutOfBoundsException ex)
		{
			LOGGER.warn(MessageFormat.format("Failed to load in current morph item, demorphing player {0}.", owner.getGameProfile().getName()));
		}
	
		instance.getFavouriteList().deserialize(capTag.getCompound("favouriteList"));
		
		instance.setLastAggroDuration(capTag.getInt("aggroDuration"));
		
		instance.setAbilitySerializationContext(AbilitySerializationContext.deserialize(capTag.getCompound("savableAbilities")));
	}
}
