package de.budschie.bmorph.morph.functionality;

import java.util.function.Consumer;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttackAbility extends AbstractEventAbility
{
	private Consumer<LivingHurtEvent> attackCode;
	
	public AttackAbility(Consumer<LivingHurtEvent> attackCode)
	{
		this.attackCode = attackCode;
	}
	
	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event)
	{
		if(event.getSource().getTrueSource() != null && trackedPlayers.contains(event.getSource().getTrueSource().getUniqueID()))
		{
			attackCode.accept(event);
		}
	}
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
