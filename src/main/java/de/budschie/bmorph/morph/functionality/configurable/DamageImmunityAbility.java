package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DamageImmunityAbility extends AbstractEventAbility
{
	public static final Codec<DamageImmunityAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.STRING.fieldOf("immune_to_damage").forGetter(DamageImmunityAbility::getImmuneTo))
			.apply(instance, DamageImmunityAbility::new));
	
	private String immuneTo;
	
	public DamageImmunityAbility(String immuneTo)
	{
		this.immuneTo = immuneTo;
	}
	
	public String getImmuneTo()
	{
		return immuneTo;
	}
	
	@SubscribeEvent
	public void onPlayerTakingDamage(LivingDamageEvent event)
	{
		if(event.getSource().getDamageType().equals(immuneTo) && event.getEntity() instanceof PlayerEntity && trackedPlayers.contains(event.getEntityLiving().getUniqueID()))
		{
			event.setCanceled(true);
		}
	}
}