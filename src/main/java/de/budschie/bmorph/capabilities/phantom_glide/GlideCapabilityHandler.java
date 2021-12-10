package de.budschie.bmorph.capabilities.phantom_glide;

import java.util.function.Consumer;

import de.budschie.bmorph.network.GlideStatusChange;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

@EventBusSubscriber
public class GlideCapabilityHandler
{	
	private static void changeStatusServer(Player player, Consumer<IGlideCapability> consumer)
	{
		player.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
		{
			consumer.accept(cap);
			
			synchronizeWithClients(player);
			synchronizeWithClient(player, (ServerPlayer) player);
		});
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			event.player.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
			{
//				if(cap.getGlideStatus() == GlideStatus.CHARGE)
//				{
//					cap.setChargeTime(cap.getChargeTime() - 1);
//					
//					if(cap.getChargeTime() <= 0)
//						cap.setGlideStatus(GlideStatus.GLIDE);
//				}
				
				if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN)
				{
					cap.setTransitionTime(cap.getTransitionTime() - 1);
					
					if(cap.getTransitionTime() <= 0)
					{
						// Reset the transition time.
						cap.setTransitionTime(cap.getMaxTransitionTime());
						
						// After transitioning, start charging
						cap.setGlideStatus(GlideStatus.CHARGE);
					}
				}
				else if(cap.getGlideStatus() == GlideStatus.CHARGE)
				{
					cap.setChargeTime(cap.getChargeTime() - 1);
					
					if(cap.getChargeTime() <= 0)
						cap.setGlideStatus(GlideStatus.CHARGE_TRANSITION_OUT);
				}
				else if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
				{
					cap.setTransitionTime(cap.getTransitionTime() - 1);
					
					if(cap.getTransitionTime() <= 0)
					{
						// After transitioning, start gliding
						cap.setGlideStatus(GlideStatus.GLIDE);
					}
				}
			});
		}
	}	
	public static void glideServer(Player player)
	{
		changeStatusServer(player, glide -> glide.glide());
	}
	
	public static void chargeServer(Player player, int maxChargeTime, ChargeDirection direction)
	{
		changeStatusServer(player, charge -> charge.charge(maxChargeTime, direction));
	}
	
	public static void startChargingServer(Player player, int transitionTime, int maxChargeTime, ChargeDirection direction)
	{
		changeStatusServer(player, transitionIn -> transitionIn.transitionIn(transitionTime, maxChargeTime, direction));
	}
	
	public static void stopChargingServer(Player player)
	{
		changeStatusServer(player, transitionOut -> transitionOut.transitionOut());
	}
	
	public static void standardServer(Player player)
	{
		changeStatusServer(player, standard -> standard.standard());
	}
	
	public static void synchronizeWithClient(Player toSynchronize, ServerPlayer with)
	{
		toSynchronize.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> with),
					new GlideStatusChange.GlideStatusChangePacket(toSynchronize.getUUID(), cap.getGlideStatus(), cap.getChargeTime(), cap.getMaxChargeTime(), cap.getTransitionTime(), cap.getChargeDirection()));
		});
	}
	
	public static void synchronizeWithClients(Player toSynchronize)
	{
		toSynchronize.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> toSynchronize),
					new GlideStatusChange.GlideStatusChangePacket(toSynchronize.getUUID(), cap.getGlideStatus(), cap.getChargeTime(), cap.getMaxChargeTime(), cap.getTransitionTime(), cap.getChargeDirection()));
		});
	}
}
