package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphStateMachineChangedSync.MorphStateMachineChangedSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent.Context;

public class MorphStateMachineChangedSync implements ISimpleImplPacket<MorphStateMachineChangedSyncPacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(MorphStateMachineChangedSyncPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getPlayer());
		buffer.writeInt(packet.changes.size());
		for(Entry<String, String> change : packet.changes.entrySet())
		{
			buffer.writeUtf(change.getKey());
			
			if(change.getValue() == null)
			{
				buffer.writeBoolean(true);
				continue;
			}
			
			buffer.writeUtf(change.getValue());
		}
	}

	@Override
	public MorphStateMachineChangedSyncPacket decode(FriendlyByteBuf buffer)
	{
		UUID player = buffer.readUUID();
		int size = buffer.readInt();
		HashMap<String, String> changes = new HashMap<>();
		
		for(int i = 0; i < size; i++)
		{
			String key = buffer.readUtf();
			
			// String is empty, record null and move along
			if(buffer.readBoolean())
			{
				changes.put(key, null);
				continue;
			}
			
			changes.put(key, buffer.readUtf());
		}
		
		return new MorphStateMachineChangedSyncPacket(changes, player);
	}

	@Override
	public void handle(MorphStateMachineChangedSyncPacket packet, Supplier<Context> ctx)
	{
		ctx.get().enqueueWork(() ->
		{
			Player player = Minecraft.getInstance().level.getPlayerByUUID(packet.getPlayer());
			
			if(player == null)
			{
				LOGGER.warn(MessageFormat.format("Changes of the morph state for player {} could not be applied as this player is not known to this client.", packet.getPlayer().toString()));
				ctx.get().setPacketHandled(true);
				return;
			}
			
			MorphUtil.getCapOrNull(player).createRecordedChangesFromPacket(packet).applyChanges();
			
			ctx.get().setPacketHandled(true);
		});
	}

	public static class MorphStateMachineChangedSyncPacket
	{
		private HashMap<String, String> changes;
		private UUID player;
		
		public MorphStateMachineChangedSyncPacket(HashMap<String, String> changes, UUID player)
		{
			this.changes = changes;
			this.player = player;
		}
		
		public HashMap<String, String> getChanges()
		{
			return changes;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
	}
}
