package de.budschie.bmorph.capabilities.phantom_glide;

import java.util.function.Consumer;

import de.budschie.bmorph.capabilities.common.CommonCapabilityHandler;
import de.budschie.bmorph.network.GlideStatusChange.GlideStatusChangePacket;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class GlideCapabilityHandler extends CommonCapabilityHandler<IGlideCapability, GlideStatusChangePacket>
{	
	public static final GlideCapabilityHandler INSTANCE = new GlideCapabilityHandler();
	
	public GlideCapabilityHandler()
	{
		super(GlideCapabilityInstance.GLIDE_CAP);
	}

	private void changeStatusServer(Player player, Consumer<IGlideCapability> consumer)
	{
		player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
		{
			consumer.accept(cap);
			
			synchronizeWithClients(player);
		});
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			event.player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
			{
				if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN)
				{
					cap.setTransitionTime(cap.getTransitionTime() - 1);
					
					if(cap.getTransitionTime() <= 0)
					{
						// Reset the transition time.
						cap.setTransitionTime(cap.getMaxTransitionTime());
						
						// After transitioning, start charging
						cap.setGlideStatus(GlideStatus.CHARGE, event.player);
					}
				}
				else if(cap.getGlideStatus() == GlideStatus.CHARGE)
				{
					cap.setChargeTime(cap.getChargeTime() - 1);
					
					if(cap.getChargeTime() <= 0)
						cap.setGlideStatus(GlideStatus.CHARGE_TRANSITION_OUT, event.player);
				}
				else if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
				{
					cap.setTransitionTime(cap.getTransitionTime() - 1);
					
					if(cap.getTransitionTime() <= 0)
					{
						// After transitioning, start gliding
						cap.setGlideStatus(GlideStatus.GLIDE, event.player);
					}
				}
			});
		}
	}	
	public void glideServer(Player player)
	{
		changeStatusServer(player, glide -> glide.glide(player));
	}
	
	public void chargeServer(Player player, int maxChargeTime, ChargeDirection direction)
	{
		changeStatusServer(player, charge -> charge.charge(maxChargeTime, direction, player));
	}
	
	public void startChargingServer(Player player, int transitionTime, int maxChargeTime, ChargeDirection direction)
	{
		changeStatusServer(player, transitionIn -> transitionIn.transitionIn(transitionTime, maxChargeTime, direction, player));
	}
	
	public void stopChargingServer(Player player)
	{
		changeStatusServer(player, transitionOut -> transitionOut.transitionOut(player));
	}
	
	public void standardServer(Player player)
	{
		changeStatusServer(player, standard -> standard.standard(player));
	}

	@Override
	protected GlideStatusChangePacket createPacket(Player player, IGlideCapability capability)
	{
		return new GlideStatusChangePacket(capability.getGlideStatus(), capability.getChargeTime(), capability.getMaxChargeTime(), capability.getTransitionTime(), capability.getChargeDirection());
	}
}
