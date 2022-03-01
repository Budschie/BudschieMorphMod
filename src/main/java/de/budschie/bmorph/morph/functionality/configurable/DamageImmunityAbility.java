package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DamageImmunityAbility extends Ability
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
	public void onPlayerTakingDamage(LivingAttackEvent event)
	{
		if(event.getSource().getMsgId().equals(immuneTo) && event.getEntity() instanceof Player && trackedPlayers.contains(event.getEntityLiving().getUUID()))
		{
			event.setCanceled(true);
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}