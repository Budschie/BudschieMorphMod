package de.budschie.bmorph.render_handler;

import java.util.function.Consumer;

import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.util.ProtectedMethodAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class SoundSynchronizer
{
	private static final ProtectedMethodAccess<Entity, SoundEvent> GET_SWIM_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_5501_");
	private static final ProtectedMethodAccess<Entity, SoundEvent> GET_SWIM_SPLASH_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_5509_");
	private static final ProtectedMethodAccess<Entity, SoundEvent> GET_SWIM_HIGH_SPEED_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_5508_");
	
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_DEATH_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_5592_");

	@SubscribeEvent
	public static void onPlaySound(PlaySoundAtEntityEvent event)
	{
		if(event.getEntity() != null && event.getEntity().getLevel().isClientSide() && event.getEntity() instanceof Player player)
		{
			// Check if player is morphed
			LazyOptional<IRenderDataCapability> renderDataOpt = player.getCapability(RenderDataCapabilityProvider.RENDER_CAP);
			
			if(renderDataOpt.isPresent())
			{
				IRenderDataCapability cap = renderDataOpt.resolve().get();
				
				Entity entity = cap.getOrCreateCachedEntity(player);
				
				Consumer<SoundEvent> soundSetter = soundEvent -> event.setSound(soundEvent);
				replaceSound(player, entity, event.getSound(), GET_SWIM_SOUND, soundSetter);
				replaceSound(player, entity, event.getSound(), GET_SWIM_SPLASH_SOUND, soundSetter);
				replaceSound(player, entity, event.getSound(), GET_SWIM_HIGH_SPEED_SOUND, soundSetter);
				
				if(entity instanceof LivingEntity living)
				{
					replaceSound(player, living, event.getSound(), GET_DEATH_SOUND, soundSetter);
				}
			}
		}
	}
	
	private static void replaceSound(Player player, Entity entity, SoundEvent currentSound, ProtectedMethodAccess<Entity, SoundEvent> soundGetter, Consumer<SoundEvent> soundSetter)
	{
		if(currentSound == soundGetter.getValue(player))
		{
			soundSetter.accept(soundGetter.getValue(entity));
		}
	}
	
	private static void replaceSound(Player player, LivingEntity entity, SoundEvent currentSound, ProtectedMethodAccess<LivingEntity, SoundEvent> soundGetter, Consumer<SoundEvent> soundSetter)
	{
		if(currentSound == soundGetter.getValue(player))
		{
			soundSetter.accept(soundGetter.getValue(entity));
		}
	}
}
