package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InstaDeathOnCookieAbility extends AbstractEventAbility
{
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
	}
	
	@SubscribeEvent
	public void onEat(LivingEntityUseItemEvent.Finish event)
	{
		if(trackedPlayers.contains(event.getEntityLiving().getUniqueID()))
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			if(event.getItem().getItem() == Items.COOKIE)
			{
				player.attackEntityFrom(DamageSource.causeBedExplosionDamage(), 420000000);
				
				if(player.getShouldBeDead())
					player.sendMessage(new StringTextComponent(TextFormatting.RED + "I have told you several times that you should not eat cookies."), Util.DUMMY_UUID);
			}
		}
	}
}
