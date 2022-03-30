package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphRequestMorphIndexChange.RequestMorphIndexChangePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphRequestMorphIndexChange implements ISimpleImplPacket<RequestMorphIndexChangePacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(RequestMorphIndexChangePacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.requestedIndex);
	}

	@Override
	public RequestMorphIndexChangePacket decode(FriendlyByteBuf buffer)
	{
		return new RequestMorphIndexChangePacket(buffer.readInt());
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
				
				if(packet.getRequestedIndex() == -1)
				{
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), ctx.get().getSender());
				}
				else if(packet.getRequestedIndex() >= resolved.getMorphList().getMorphArrayList().size() || packet.getRequestedIndex() < 0)
				{
					LOGGER.warn(MessageFormatter.format("Player {0} with UUID {1} has tried to send invalid data!", ctx.get().getSender().getName().getString(), ctx.get().getSender().getUUID()));
				}
				else
				{
					ResourceLocation morphToRS = resolved.getMorphList().getMorphArrayList().get(packet.getRequestedIndex()).getEntityType().getRegistryName();
					boolean shouldMorph = !ConfigManager.INSTANCE.get(BlacklistData.class).isInBlacklist(morphToRS);
					
					if(shouldMorph)
						MorphUtil.morphToServer(Optional.empty(), Optional.of(packet.getRequestedIndex()), ctx.get().getSender());
					else
						ctx.get().getSender().sendMessage(new TextComponent(ChatFormatting.RED + "I'm sorry but you can't morph into " + morphToRS.toString() + " as this entity is currently blacklisted."), Util.NIL_UUID);					
				}				
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class RequestMorphIndexChangePacket
	{
		int requestedIndex;
		
		public RequestMorphIndexChangePacket(int requestedIndex)
		{
			this.requestedIndex = requestedIndex;
		}
		
		public int getRequestedIndex()
		{
			return requestedIndex;
		}
	}
}
