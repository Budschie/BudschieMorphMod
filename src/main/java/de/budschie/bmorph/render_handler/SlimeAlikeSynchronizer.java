package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.util.ProtectedMethodAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class SlimeAlikeSynchronizer
{
	private static final ProtectedMethodAccess<Slime, Float> GET_SOUND_PITCH = new ProtectedMethodAccess<>(Slime.class, "m_33642_");
	private static final ProtectedMethodAccess<Slime, Float> GET_SOUND_VOLUME = new ProtectedMethodAccess<>(Slime.class, "m_6121_");
	private static final ProtectedMethodAccess<Slime, SoundEvent> GET_JUMP_SOUND = new ProtectedMethodAccess<>(Slime.class, "m_7903_");
	
	@SubscribeEvent
	public static void onPlayerJump(LivingJumpEvent event)
	{
		if(event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof Player player)
		{
			// Get render entity
			LazyOptional<IRenderDataCapability> renderDataOpt = player.getCapability(RenderDataCapabilityProvider.RENDER_CAP);
			
			if(renderDataOpt.isPresent())
			{
				IRenderDataCapability cap = renderDataOpt.resolve().get();
				
				Entity entity = cap.getOrCreateCachedEntity(player);
				
				if(entity instanceof Slime slime && slime.doPlayJumpSound())
				{
					slime.playSound(GET_JUMP_SOUND.getValue(slime), GET_SOUND_VOLUME.getValue(slime), GET_SOUND_PITCH.getValue(slime));
				}
			}
		}
	}
}
