package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import com.ibm.icu.text.MessageFormat;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import net.minecraft.nbt.CompoundTag;
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
		packet.getRequestedItem().ifPresent(buffer::writeNbt);
	}

	@Override
	public RequestMorphIndexChangePacket decode(FriendlyByteBuf buffer)
	{
		boolean requestedItemPresent = buffer.readBoolean();		
		Optional<CompoundTag> item = Optional.empty();
		
		if(requestedItemPresent)
		{
			item = Optional.of(buffer.readNbt());
		}
		
		return RequestMorphIndexChangePacket.ofNbt(item);
	}

	@Override
	public void handle(RequestMorphIndexChangePacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			boolean exceptionRaised = false;
			Optional<MorphItem> morphItem = Optional.empty();
			
			try
			{
				if(packet.getRequestedItem().isPresent())
				{
					morphItem = Optional.of(MorphHandler.deserializeMorphItem(packet.getRequestedItem().get()));
				}
//					
//					if(packet.getRequestedIndex() == -1)
//					{
//						MorphUtil.morphToServer(Optional.empty(), Optional.empty(), ctx.get().getSender());
//					}
//					else if(packet.getRequestedIndex() >= resolved.getMorphList().getMorphArrayList().size() || packet.getRequestedIndex() < 0)
//					{
//						LOGGER.warn(MessageFormatter.format("Player {0} with UUID {1} has tried to send invalid data!", ctx.get().getSender().getName().getString(), ctx.get().getSender().getUUID()));
//					}
//					else
//					{
//						ResourceLocation morphToRS = resolved.getMorphList().getMorphArrayList().get(packet.getRequestedIndex()).getEntityType().getRegistryName();
//						boolean shouldMorph = !ConfigManager.INSTANCE.get(BlacklistData.class).isInBlacklist(morphToRS);
//						
//						if(shouldMorph)
//							MorphUtil.morphToServer(Optional.empty(), Optional.of(packet.getRequestedIndex()), ctx.get().getSender());
//						else
//							ctx.get().getSender().sendMessage(new TextComponent(ChatFormatting.RED + "I'm sorry but you can't morph into " + morphToRS.toString() + " as this entity is currently blacklisted."), Util.NIL_UUID);					
//					}
			}
			catch(IllegalArgumentException ex)
			{
				LOGGER.warn(MessageFormat.format("Could not recognize morph item format for morph item NBT: {0}", packet.getRequestedItem().get()));
				exceptionRaised = true;
			}
			
			
			if(!exceptionRaised)
			{
				LazyOptional<IMorphCapability> cap = ctx.get().getSender().getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					IMorphCapability resolved = cap.resolve().get();
					
					if(morphItem.isPresent())
					{
						Optional<Integer> indexOfItem = resolved.getMorphList().indexOf(morphItem.get());
						
						if(indexOfItem.isPresent())
						{
							MorphUtil.morphToServer(Optional.empty(), indexOfItem, ctx.get().getSender());
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
						MorphUtil.morphToServer(Optional.empty(), Optional.empty(), ctx.get().getSender());
					}
				}
			}
						
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class RequestMorphIndexChangePacket
	{
		private Optional<CompoundTag> requestedItem;
		
		private RequestMorphIndexChangePacket()
		{
		}
		
		public Optional<CompoundTag> getRequestedItem()
		{
			return requestedItem;
		}
		
		public static RequestMorphIndexChangePacket ofNbt(Optional<CompoundTag> tag)
		{
			RequestMorphIndexChangePacket packet = new RequestMorphIndexChangePacket();
			packet.requestedItem = tag;
			return packet;
		}
		
		public static RequestMorphIndexChangePacket ofMorphItem(Optional<MorphItem> morphItem)
		{
			RequestMorphIndexChangePacket packet = new RequestMorphIndexChangePacket();
			packet.requestedItem = morphItem.map(MorphItem::serialize);
			return packet;
		}
	}
}
