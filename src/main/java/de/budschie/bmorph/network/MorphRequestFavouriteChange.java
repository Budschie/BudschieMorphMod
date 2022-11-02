package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.FavouriteList;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.network.MorphRequestFavouriteChange.MorphRequestFavouriteChangePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphRequestFavouriteChange implements ISimpleImplPacket<MorphRequestFavouriteChangePacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(MorphRequestFavouriteChangePacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.shouldAdd());
		buffer.writeUUID(packet.getMorphItemKey());
	}

	@Override
	public MorphRequestFavouriteChangePacket decode(FriendlyByteBuf buffer)
	{
		return new MorphRequestFavouriteChangePacket(buffer.readBoolean(), buffer.readUUID());
	}

	@Override
	public void handle(MorphRequestFavouriteChangePacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			LazyOptional<IMorphCapability> cap = ctx.get().getSender().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
						
				boolean hasMorphItem = resolved.getMorphList().contains(packet.getMorphItemKey());
				
				if(hasMorphItem)
				{
					FavouriteList favouriteList = resolved.getFavouriteList();
					
					if(packet.shouldAdd())
						favouriteList.addFavourite(packet.getMorphItemKey());
					else
						favouriteList.removeFavourite(packet.getMorphItemKey());
				}
				else
				{
					LOGGER.warn(MessageFormat.format("Player {0} asked to favourite the morph item with the key {1}. This morph item does not exist on the server. The request will be ignored.", ctx.get().getSender().getGameProfile().getName(), packet.getMorphItemKey()));
				}
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class MorphRequestFavouriteChangePacket
	{
		private boolean add;
		private UUID morphItemKey;
		
		public MorphRequestFavouriteChangePacket(boolean add, UUID morphItemKey)
		{
			this.add = add;
			this.morphItemKey = morphItemKey;
		}
		
		public boolean shouldAdd()
		{
			return add;
		}
		
		public UUID getMorphItemKey()
		{
			return morphItemKey;
		}
	}
}
