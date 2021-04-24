package de.budschie.bmorph.morph.functionality;

import java.util.function.BiConsumer;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class NightVisionAbility extends PassiveTickAbility
{

	public NightVisionAbility()
	{
		super(20, (player, item) ->
		{
			player.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, 500, 1, true, false, false, null));
		});
	}
	
	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		player.removeActivePotionEffect(Effects.NIGHT_VISION);
		super.disableAbility(player, disabledItem);
	}
}
