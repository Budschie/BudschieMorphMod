package de.budschie.bmorph.capabilities.pufferfish;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class PufferfishCapabilityHandler
{
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.side == LogicalSide.CLIENT && event.phase == Phase.END)
		{
			event.player.getCapability(PufferfishCapabilityAttacher.PUFFER_CAP).ifPresent(cap ->
			{
				if(cap.getPuffTime() > 0)
					cap.setPuffTime(cap.getPuffTime() - 1);
			});
		}
	}
}
