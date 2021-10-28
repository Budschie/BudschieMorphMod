package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InstaDeathOnCookieAbility extends AbstractEventAbility
{
	private Codec<InstaDeathOnCookieAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.BOOL.optionalFieldOf("painful", false).forGetter(InstaDeathOnCookieAbility::isPainfulDeath))
			.apply(instance, InstaDeathOnCookieAbility::new));
	
	private boolean isPainfulDeath;
	
	public InstaDeathOnCookieAbility(boolean isPainfulDeath)
	{
		this.isPainfulDeath = isPainfulDeath;
	}
	
	public boolean isPainfulDeath()
	{
		return isPainfulDeath;
	}
	
	@SubscribeEvent
	public void onEat(LivingEntityUseItemEvent.Finish event)
	{
		if(isTracked(event.getEntity()))
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			if(event.getItem().getItem() == Items.COOKIE)
			{
				if(isPainfulDeath)
				{
					// JEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEESSSSSSSSSSSSSSSSSSSSSSSSSSSSSS u found easteregg
					player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 100000, 10));
					player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 100000, 10));
					player.addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 10000, 100));
					player.addPotionEffect(new EffectInstance(Effects.POISON, 1000, 2));
					player.addPotionEffect(new EffectInstance(Effects.WITHER, 1000, 0));
					player.setGlowing(true);
					player.setMotion(0, 10, 0);
					
					player.sendMessage(new StringTextComponent(TextFormatting.RED + "H4h4 u d3d s00n"), Util.DUMMY_UUID);
				}
				else
				{
					player.attackEntityFrom(DamageSource.causeBedExplosionDamage(), 420000000);
					
					if(player.getShouldBeDead())
						player.sendMessage(new StringTextComponent(TextFormatting.RED + "I have told you several times that you should not eat cookies."), Util.DUMMY_UUID);
					else
						player.sendMessage(new StringTextComponent(TextFormatting.RED + "How the.... what? You survived?"), Util.DUMMY_UUID);
				}
			}
		}
	}
}
