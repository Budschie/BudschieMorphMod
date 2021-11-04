package de.budschie.bmorph.capabilities.pufferfish;

import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.PufferfishPuff;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class PufferfishCapabilityHandler
{
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			event.player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
			{
				if(cap.getPuffTime() > 0)
					cap.setPuffTime(cap.getPuffTime() - 1);
			});
		}
	}
	
	public static void puffServer(PlayerEntity player, int duration)
	{
		player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
		{
			cap.puff(duration);	
			
			synchronizeWithClients(player);
			synchronizeWithClient(player, (ServerPlayerEntity) player);
		});
	}
	
	public static void synchronizeWithClient(PlayerEntity toSynchronize, ServerPlayerEntity with)
	{
		toSynchronize.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> with),
					new PufferfishPuff.PufferfishPuffPacket(cap.getOriginalPuffTime(), cap.getPuffTime(), toSynchronize.getUniqueID()));
		});
	}
	
	public static void synchronizeWithClients(PlayerEntity toSynchronize)
	{
		toSynchronize.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> toSynchronize),
					new PufferfishPuff.PufferfishPuffPacket(cap.getOriginalPuffTime(), cap.getPuffTime(), toSynchronize.getUniqueID()));
		});
	}
}
