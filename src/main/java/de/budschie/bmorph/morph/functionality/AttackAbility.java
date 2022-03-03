package de.budschie.bmorph.morph.functionality;

import java.util.function.Consumer;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AttackAbility extends Ability
{
	private Consumer<LivingHurtEvent> attackCode;
	
	public AttackAbility(Consumer<LivingHurtEvent> attackCode)
	{
		this.attackCode = attackCode;
	}
	
	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event)
	{
		if(event.getSource().getEntity() != null && trackedPlayers.contains(event.getSource().getEntity().getUUID()))
		{
			attackCode.accept(event);
		}
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
