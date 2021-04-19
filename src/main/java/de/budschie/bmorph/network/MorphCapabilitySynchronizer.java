package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkEvent;

public class MorphCapabilitySynchronizer
{
	public static void encode(MorphPacket packet, PacketBuffer buffer)
	{
		buffer.writeUniqueId(packet.player);
		buffer.writeCompoundTag(packet.morphList.serialize());
		buffer.writeBoolean(packet.entityData.isPresent());
		packet.getEntityData().ifPresent(data -> buffer.writeCompoundTag(data.serialize()));
	}
	
	public static MorphPacket decode(PacketBuffer buffer)
	{
		UUID playerUUID = buffer.readUniqueId();
		
		// Hmmm yeah the floor is made out of floor
		CompoundNBT morphlistNotDeserialized = buffer.readCompoundTag();
		
		Optional<CompoundNBT> toMorph = Optional.empty();
		
		if(buffer.readBoolean())
			toMorph = Optional.of(buffer.readCompoundTag());
		
		MorphList deserialized = new MorphList();
		deserialized.deserialize(morphlistNotDeserialized);
		
		if(toMorph.isPresent())
			return new MorphPacket(Optional.of(MorphHandler.deserializeMorphItem(toMorph.get())), deserialized, playerUUID);
		else
			return new MorphPacket(Optional.empty(), deserialized, playerUUID);
	}
	
	public static void handle(MorphPacket message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = Minecraft.getInstance().getConnection().getWorld().getPlayerByUuid(message.getPlayer()).getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				if(message.entityData.isPresent())
					cap.resolve().get().setCurrentMorph(Optional.of(message.entityData.get()));
				else
					cap.resolve().get().setCurrentMorph(Optional.empty());
				
				cap.resolve().get().setMorphList(message.morphList);
			}
		});
	}
	
	public static class MorphPacket
	{
		private Optional<MorphItem> entityData;
		private MorphList morphList;
		private UUID player;
		
		public MorphPacket(Optional<MorphItem> entityData, MorphList morphList, UUID player)
		{
			this.entityData = entityData;
			this.player = player;
			this.morphList = morphList;
		}
		
		public void setEntityData(Optional<MorphItem> entityData)
		{
			this.entityData = entityData;
		}
		
		public void setPlayer(UUID player)
		{
			this.player = player;
		}
		
		public void setMorphList(MorphList morphList)
		{
			this.morphList = morphList;
		}
		
		public Optional<MorphItem> getEntityData()
		{
			return entityData;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
		
		public MorphList getMorphList()
		{
			return morphList;
		}
	}
}
