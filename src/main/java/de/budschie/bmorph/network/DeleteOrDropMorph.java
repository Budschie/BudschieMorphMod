package de.budschie.bmorph.network;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.entity.MorphEntity;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.DeleteOrDropMorph.DeleteOrDropMorphPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DeleteOrDropMorph implements ISimpleImplPacket<DeleteOrDropMorphPacket>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void encode(DeleteOrDropMorphPacket packet, FriendlyByteBuf buffer)
	{
		buffer.writeUUID(packet.getMorphItemKey());
		buffer.writeBoolean(packet.shouldDrop());
	}

	@Override
	public DeleteOrDropMorphPacket decode(FriendlyByteBuf buffer)
	{
		return new DeleteOrDropMorphPacket(buffer.readUUID(), buffer.readBoolean());
	}

	@Override
	public void handle(DeleteOrDropMorphPacket packet, Supplier<Context> ctx)
	{		
		ctx.get().enqueueWork(() ->
		{
			// Just do nothing if we don't allow dropping or deleting morph items
			if((packet.drop && !ServerLifecycleHooks.getCurrentServer().getGameRules().getBoolean(BMorphMod.ALLOW_MORPH_DROPPING)) || (!packet.drop && !ServerLifecycleHooks.getCurrentServer().getGameRules().getBoolean(BMorphMod.ALLOW_MORPH_DELETION)))
			{
				ctx.get().setPacketHandled(true);
				return;
			}

			MorphUtil.processCap(ctx.get().getSender(), cap ->
			{
				Optional<MorphItem> morphItem = cap.getMorphList().getMorphByUUID(packet.getMorphItemKey());
				
				if(morphItem.isPresent())
				{
					if(cap.getCurrentMorph().isPresent() && cap.getCurrentMorph().get().equals(morphItem.get()))
						MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_DELETING_OR_DROPPING_MORPH.get(), ctx.get().getSender());
					
					cap.getMorphList().removeMorphItem(packet.getMorphItemKey());
					cap.syncMorphRemoval(packet.getMorphItemKey());
					
					if(packet.shouldDrop())
					{
						Level level = ctx.get().getSender().getLevel();
						// Nice
						MorphEntity morphEntity = new MorphEntity(level, morphItem.get(), 69);
						morphEntity.setPos(ctx.get().getSender().position());
						level.addFreshEntity(morphEntity);
					}
				}
				else
				{
					LOGGER.warn(MessageFormat.format("Player {0} requested the removal of the morph with the key {1}, but this key does not exist on the server. Please report this issue.", ctx.get().getSender().getGameProfile().getName(), packet.getMorphItemKey()));
				}
				
			});
			
			ctx.get().setPacketHandled(true);
		});
	}
	
	public static class DeleteOrDropMorphPacket
	{
		private UUID morphItemKey;
		private boolean drop;
		
		public DeleteOrDropMorphPacket(UUID morphItemKey, boolean drop)
		{
			this.morphItemKey = morphItemKey;
			this.drop = drop;
		}
		
		public UUID getMorphItemKey()
		{
			return morphItemKey;
		}
		
		public boolean shouldDrop()
		{
			return drop;
		}
	}
}
