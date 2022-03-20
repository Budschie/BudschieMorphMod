package de.budschie.bmorph.capabilities.bossbar;

import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class BossbarCapabilityUpdater
{
	@SubscribeEvent
	public static void updateBossbar(PlayerTickEvent event)
	{
		if(event.side == LogicalSide.SERVER && event.phase == Phase.END)
		{
			event.player.getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(bossbarCap ->
			{
				bossbarCap.getBossbar().ifPresent(bossbar -> bossbar.setProgress(event.player.getHealth() / event.player.getMaxHealth()));
			});
		}
	}
}
