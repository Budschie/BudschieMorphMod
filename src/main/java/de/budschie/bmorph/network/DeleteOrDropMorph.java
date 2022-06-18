package de.budschie.bmorph.network;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.entity.MorphEntity;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
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
		buffer.writeInt(packet.getIndex());
		buffer.writeBoolean(packet.shouldDrop());
	}

	@Override
	public DeleteOrDropMorphPacket decode(FriendlyByteBuf buffer)
	{
		return new DeleteOrDropMorphPacket(buffer.readInt(), buffer.readBoolean());
	}

	@Override
	public void handle(DeleteOrDropMorphPacket packet, Supplier<Context> ctx)
	{
		// Just do nothing if we don't allow dropping or deleting morph items
		if((packet.drop && !ServerLifecycleHooks.getCurrentServer().getGameRules().getBoolean(BMorphMod.ALLOW_MORPH_DROPPING)) || (!packet.drop && !ServerLifecycleHooks.getCurrentServer().getGameRules().getBoolean(BMorphMod.ALLOW_MORPH_DELETION)))
			return;
		
		ctx.get().enqueueWork(() ->
		{
			MorphUtil.processCap(ctx.get().getSender(), cap ->
			{
				if(packet.getIndex() >= cap.getMorphList().getMorphArrayList().size() || packet.getIndex() < 0)
				{
					LOGGER.warn(String.format(
							"Client %s tried to parse in bad morph item index when sending the DeleteOrDropMorph packet; we got %s and expected a number from 0 - %s (non-inclusive).",
							ctx.get().getSender().getName(),
							packet.getIndex(),
							cap.getMorphList().getMorphArrayList().size()
							));
					
					return;
				}
				
				MorphItem morph = cap.getMorphList().getMorphArrayList().get(packet.getIndex());
				
				if(cap.getCurrentMorphIndex().isPresent() && morph == cap.getMorphList().getMorphArrayList().get(cap.getCurrentMorphIndex().get()))
					MorphUtil.morphToServer(Optional.empty(), Optional.empty(), ctx.get().getSender());
				
				cap.getMorphList().removeFromMorphList(packet.getIndex());
				cap.syncMorphRemoval(packet.getIndex());
				
				if(packet.shouldDrop())
				{
					Level level = ctx.get().getSender().getLevel();
					// Nice
					MorphEntity morphEntity = new MorphEntity(level, morph, 69);
					morphEntity.setPos(ctx.get().getSender().position());
					level.addFreshEntity(morphEntity);
				}
				
				ctx.get().setPacketHandled(true);
			});			
		});
	}
	
	public static class DeleteOrDropMorphPacket
	{
		private int index;
		private boolean drop;
		
		public DeleteOrDropMorphPacket(int index, boolean drop)
		{
			this.index = index;
			this.drop = drop;
		}
		
		public int getIndex()
		{
			return index;
		}
		
		public boolean shouldDrop()
		{
			return drop;
		}
	}
}
