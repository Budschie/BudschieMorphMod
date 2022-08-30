package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphItemDisabled.MorphItemDisabledPacket;
import de.budschie.bmorph.util.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphItemDisabled implements ISimpleImplPacket<MorphItemDisabledPacket>
{
	private static final int MAX_ARRAY_SIZE = Short.MAX_VALUE;
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(MorphItemDisabledPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeInt(packet.getDisabledFor());

		buffer.writeInt(packet.keys.length);
		for(UUID uuid : packet.keys)
			buffer.writeUUID(uuid);
	}

	@Override
	public MorphItemDisabledPacket decode(FriendlyByteBuf buffer)
	{
		int timeToDisableFor = buffer.readInt();
		int length = buffer.readInt();
		
		if(length > MAX_ARRAY_SIZE)
		{
			throw new IllegalArgumentException("The amount of morph items that shall be disabled shall not exceed the value of 32767. If you see this error, please report it to the mod author.");
		}
		
		UUID[] toDisable = new UUID[length];
		
		for(int i = 0; i < length; i++)
			toDisable[i] = buffer.readUUID();
		
		return new MorphItemDisabledPacket(timeToDisableFor, toDisable);
	}

	@Override
	public void handle(MorphItemDisabledPacket packet, Supplier<Context> ctx)
	{
		// Why tf doesn't this work (but other shit like this did)?
		ctx.get().enqueueWork(() ->
		{
			IMorphCapability cap = MorphUtil.getCapOrNull(ClientUtils.getPlayer());
			
			if(cap != null)
			{
				for(UUID key : packet.getKeys())
				{
					Optional<MorphItem> correspondingItem = cap.getMorphList().getMorphByUUID(key);
					
					if(correspondingItem.isEmpty())
					{
						LOGGER.warn(MessageFormat.format("Server said that a morph item should be disabled that doesn't even exist. UUID of morph item: {0}", key));
					}
					else
					{
						// cap.getMorphList().getMorphArrayList().get(index).disable(packet.getDisabledFor());
						correspondingItem.get().disable(packet.getDisabledFor());
					}
				}
			}
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class MorphItemDisabledPacket
	{
		private UUID[] keys;
		private int disabledFor;
				
		public MorphItemDisabledPacket(int disabledFor, UUID...keys)
		{
			this.disabledFor = disabledFor;
			this.keys = keys;
		}

		public UUID[] getKeys()
		{
			return keys;
		}
		
		public int getDisabledFor()
		{
			return disabledFor;
		}
	}
}
