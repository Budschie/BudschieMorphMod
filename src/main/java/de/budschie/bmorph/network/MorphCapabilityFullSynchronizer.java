package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphList;
import de.budschie.bmorph.morph.MorphReason;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphCapabilityFullSynchronizer.MorphPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent;

public class MorphCapabilityFullSynchronizer implements ISimpleImplPacket<MorphPacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(MorphPacket packet, FriendlyByteBuf buffer)
	{		
		buffer.writeUUID(packet.player);
		packet.morphList.serializePacket(buffer);
		packet.favouriteList.serializePacket(buffer);
		buffer.writeBoolean(packet.morphItemNbt.isPresent());
		packet.getMorphItemNbt().ifPresent(data -> buffer.writeNbt(data));
		buffer.writeResourceLocation(packet.getReason());
		
		buffer.writeInt(packet.getAbilities().size());
		
		for(String str : packet.getAbilities())
			buffer.writeUtf(str);
	}
	
	@Override
	public MorphPacket decode(FriendlyByteBuf buffer)
	{
		UUID playerUUID = buffer.readUUID();
		
		// Hmmm yeah the floor is made out of floor
		MorphList morphList = new MorphList();
		morphList.deserializePacket(buffer);
		
		FavouriteList favouriteList = new FavouriteList(morphList);
		favouriteList.deserializePacket(buffer);
		
		Optional<CompoundTag> toMorph = Optional.empty();
		
		boolean hasMorph = buffer.readBoolean();
		
		if(hasMorph)
		{
			toMorph = Optional.of(buffer.readNbt());
		}
		
		ResourceLocation reason = buffer.readResourceLocation();
		
		int amountOfAbilities = buffer.readInt();
		
		ArrayList<String> abilities = new ArrayList<>(amountOfAbilities);
		
		for(int i = 0; i < amountOfAbilities; i++)
			abilities.add(buffer.readUtf());
		
		return new MorphPacket(toMorph, reason, morphList, favouriteList, abilities, playerUUID);
	}
	
	@Override
	public void handle(MorphPacket packet, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			if(Minecraft.getInstance().level != null)
			{
				Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
				
				MorphReason reason;
				
				MorphReason registryReason = MorphReasonRegistry.REGISTRY.get().getValue(packet.getReason());
				reason = registryReason;
					
				if(registryReason == null)
				{
					LOGGER.warn(MessageFormat.format("Unknown morph reason {0} received from server. Default reason will be used.", packet.getReason()));
				}
				
				if(player != null)
				{
					LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
					if(cap.isPresent())
					{
						IMorphCapability resolved = cap.resolve().get();
						resolved.setFavouriteList(packet.getFavouriteList());
						resolved.setMorphList(packet.getMorphList());
					}
					// MorphUtil.morphToClient(packet.getEntityData(), packet.getEntityIndex(), packet.getAbilities(), player);
					MorphUtil.morphToClient(packet.getMorphItemNbt().map(MorphHandler::deserializeMorphItem), reason == null ? MorphReasonRegistry.MORPHED_BY_COMMAND.get() : reason, packet.getAbilities(), player);
					ctx.get().setPacketHandled(true);
				}
			}
		});
	}
	
	public static class MorphPacket
	{
		private Optional<CompoundTag> morphItemNbt;
		private ResourceLocation reason;
		private MorphList morphList;
		private FavouriteList favouriteList;
		private ArrayList<String> abilities;
		private UUID player;
		
		public MorphPacket(Optional<CompoundTag> morphItemNbt, ResourceLocation reason, MorphList morphList, FavouriteList favouriteList, ArrayList<String> abilities, UUID player)
		{
			this.morphItemNbt = morphItemNbt;
			this.reason = reason;
			this.player = player;
			this.morphList = morphList;
			this.favouriteList = favouriteList;
			this.abilities = abilities;
		}
		
		public ArrayList<String> getAbilities()
		{
			return abilities;
		}
		
		public ResourceLocation getReason()
		{
			return reason;
		}
		
		public Optional<CompoundTag> getMorphItemNbt()
		{
			return morphItemNbt;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
		
		public MorphList getMorphList()
		{
			return morphList;
		}
		
		public FavouriteList getFavouriteList()
		{
			return favouriteList;
		}
	}
}
