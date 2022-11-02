package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.ProxyEntityEvent.ProxyEntityEventPacket;
import de.budschie.bmorph.tags.ModEntityTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber
public class IronGolemSynchronizer
{
	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent event)
	{		
		if(event.getSource().getEntity() instanceof Player player)
		{
			MorphUtil.processCap(player, cap ->
			{
				cap.getCurrentMorph().ifPresent(currentMorph ->
				{
					if(currentMorph.getEntityType().is(ModEntityTypeTags.IRON_GOLEM_ALIKE))
					{
						// Send byte 4 as an entity event to all nearby players so that the iron golem attack animation gets displayed correctly.
						MainNetworkChannel.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ProxyEntityEventPacket(player, (byte) 4));
					}
				});
			});
		}
	}
}
