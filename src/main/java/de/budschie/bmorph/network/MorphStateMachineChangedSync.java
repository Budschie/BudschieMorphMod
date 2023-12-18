package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.MorphStateMachine.MorphStateMachineEntry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MorphStateMachineChangedSync.MorphStateMachineChangedSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
		for(Entry<ResourceLocation, NetworkMorphStateMachineEntry> change : packet.changes.entrySet())
		{
			buffer.writeUtf(change.getKey().toString());
			
			buffer.writeBoolean(change.getValue().deltaTicks.isPresent());
			buffer.writeBoolean(change.getValue().value.isPresent());
			
			if(change.getValue().deltaTicks.isPresent())
			{
				buffer.writeInt(change.getValue().deltaTicks.get());
			}
			
			if(change.getValue().value.isPresent())
			{
				buffer.writeUtf(change.getValue().value.get());
			}
		}
	}

	@Override
	public MorphStateMachineChangedSyncPacket decode(FriendlyByteBuf buffer)
	{
		UUID player = buffer.readUUID();
		int size = buffer.readInt();
		HashMap<ResourceLocation, NetworkMorphStateMachineEntry> changes = new HashMap<>();
		
		for(int i = 0; i < size; i++)
		{
			String key = buffer.readUtf();
			
			boolean readTimestamp = buffer.readBoolean();
			boolean readValue = buffer.readBoolean();
			
			Optional<Integer> timestamp = Optional.empty();
			Optional<String> value = Optional.empty();
			
			if(readTimestamp)
			{
				timestamp = Optional.of(buffer.readInt());
			}
			
			if(readValue)
			{
				value = Optional.of(buffer.readUtf());
			}
			
			changes.put(new ResourceLocation(key), new NetworkMorphStateMachineEntry(timestamp, value));
		}
		
		return MorphStateMachineChangedSyncPacket.fromNetworkPacket(changes, player);
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

	public static record NetworkMorphStateMachineEntry(Optional<Integer> deltaTicks, Optional<String> value)
	{
		
	}
	
	public static class MorphStateMachineChangedSyncPacket
	{
		private HashMap<ResourceLocation, NetworkMorphStateMachineEntry> changes;
		private UUID player;
				
		public static MorphStateMachineChangedSyncPacket fromChanges(HashMap<ResourceLocation, MorphStateMachineEntry> changes, UUID player)
		{
			HashMap<ResourceLocation, NetworkMorphStateMachineEntry>  networkChanges = new HashMap<>();
			
			changes.forEach((key, value) ->
			{
				networkChanges.put(key, new NetworkMorphStateMachineEntry(value.getTimeElapsedSinceChange().map(tickTimestamp -> tickTimestamp.getTimeElapsed()), value.getValue()));
			});
			
			return new MorphStateMachineChangedSyncPacket(networkChanges, player);
		}
		
		public static MorphStateMachineChangedSyncPacket fromNetworkPacket(HashMap<ResourceLocation, NetworkMorphStateMachineEntry> changes, UUID player)
		{
			return new MorphStateMachineChangedSyncPacket(changes, player);
		}
		
		private MorphStateMachineChangedSyncPacket(HashMap<ResourceLocation, NetworkMorphStateMachineEntry> changes, UUID player)
		{
			this.player = player;
			this.changes = changes;
		}
		
		public HashMap<ResourceLocation, NetworkMorphStateMachineEntry> getChanges()
		{
			return changes;
		}
		
		public UUID getPlayer()
		{
			return player;
		}
	}
}
