package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class NoKnockbackAbility extends AbstractEventAbility
{
	public static final Codec<NoKnockbackAbility> CODEC = ModCodecs.newCodec(NoKnockbackAbility::new);
	
	@SubscribeEvent
	public void onLivingKnockbackEvent(LivingKnockBackEvent event)
	{
		if(isTracked(event.getEntity()))
				event.setCanceled(true);
	}
}
