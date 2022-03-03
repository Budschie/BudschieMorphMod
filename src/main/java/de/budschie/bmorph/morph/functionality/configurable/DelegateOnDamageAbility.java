package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DelegateOnDamageAbility extends Ability
{
	public static final Codec<DelegateOnDamageAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.STRING.fieldOf("delegate_on_damage").forGetter(DelegateOnDamageAbility::getDelegateOnDamage), ModCodecs.ABILITY.fieldOf("delegate_to").forGetter(DelegateOnDamageAbility::getDelegateTo))
			.apply(instance, DelegateOnDamageAbility::new));
	
	private String delegateOnDamage;
	private LazyOptional<Ability> delegateTo;
	
	public DelegateOnDamageAbility(String delegateOnDamage, LazyOptional<Ability> delegateTo)
	{
		this.delegateOnDamage = delegateOnDamage;
		this.delegateTo = delegateTo;
	}
	
	public String getDelegateOnDamage()
	{
		return delegateOnDamage;
	}
	
	public LazyOptional<Ability> getDelegateTo()
	{
		return delegateTo;
	}
	
	@SubscribeEvent
	public void onPlayerTakingDamage(LivingAttackEvent event)
	{
		if(event.getSource().getMsgId().equals(delegateOnDamage) && event.getEntity() instanceof Player player && trackedPlayers.contains(event.getEntityLiving().getUUID()))
		{
			delegateTo.resolve().get().onUsedAbility(player, MorphUtil.getCapOrNull(player).getCurrentMorph().get());
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}