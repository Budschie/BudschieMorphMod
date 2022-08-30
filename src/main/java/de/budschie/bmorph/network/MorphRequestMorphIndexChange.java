package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphRequestMorphIndexChange implements ISimpleImplPacket<RequestMorphIndexChangePacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(RequestMorphIndexChangePacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(packet.getRequestedItem().isPresent());
		packet.getRequestedItem().ifPresent(buffer::writeUUID);
	}

	@Override
	public RequestMorphIndexChangePacket decode(FriendlyByteBuf buffer)
	{
		boolean requestedItemPresent = buffer.readBoolean();		
		Optional<UUID> item = Optional.empty();
		
		if(requestedItemPresent)
		{
			item = Optional.of(buffer.readUUID());
		}
		
		return new RequestMorphIndexChangePacket(item);
	}

	@Override
	public void handle(RequestMorphIndexChangePacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{			
				LazyOptional<IMorphCapability> cap = ctx.get().getSender().getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					IMorphCapability resolved = cap.resolve().get();
					
					Optional<MorphItem> morphItem = Optional.empty();
					
					if(packet.getRequestedItem().isPresent())
					{
						morphItem = resolved.getMorphList().getMorphByUUID(packet.getRequestedItem().get());
					}
					
					if(morphItem.isPresent())
					{
						// Optional<Integer> indexOfItem = resolved.getMorphList().indexOf(morphItem.get());
						boolean hasMorph = resolved.getMorphList().contains(morphItem.get());
						
						if(hasMorph)
						{
							MorphUtil.morphToServer(morphItem, MorphReasonRegistry.MORPHED_BY_UI.get(), ctx.get().getSender());
						}
						else
						{
							LOGGER.warn(MessageFormatter.arrayFormat("Player {0} with UUID {1} tried to morph into an entity which they do not posses. Morph: {2}", 
								new Object[]
								{
									ctx.get().getSender().getGameProfile().getName(), 
									ctx.get().getSender().getStringUUID(), 
									packet.getRequestedItem().get().toString()
								}
							));
						}
					}
					else
					{
						MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_UI.get(), ctx.get().getSender());
					}
				}
						
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class RequestMorphIndexChangePacket
	{
		private Optional<UUID> requestedItem;
		
		public RequestMorphIndexChangePacket(Optional<UUID> requestedItem)
		{
			this.requestedItem = requestedItem;
		}
		
		public Optional<UUID> getRequestedItem()
		{
			return requestedItem;
		}
	}
}
