package de.budschie.bmorph.morph.functionality;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.events.WebEvent;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WebSpeedAbility extends AbstractEventAbility
{
	public static Codec<WebSpeedAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ModCodecs.VECTOR_3D.fieldOf("speed_multiplier").forGetter(WebSpeedAbility::getWebSpeedMultiplier)).apply(instance, WebSpeedAbility::new));
	
	private Vector3d webSpeedMultiplier;
	
	public WebSpeedAbility(Vector3d webSpeedMultiplier)
	{
		this.webSpeedMultiplier = webSpeedMultiplier;
	}
	
	public Vector3d getWebSpeedMultiplier()
	{
		return webSpeedMultiplier;
	}
	
	@SubscribeEvent
	public void onWeb(WebEvent event)
	{
		event.setNewWebSpeed(webSpeedMultiplier);
	}
}
