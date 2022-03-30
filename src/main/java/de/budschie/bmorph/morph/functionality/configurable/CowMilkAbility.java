package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CowMilkAbility extends Ability
{
	public static final Codec<CowMilkAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("food_level_cost").forGetter(CowMilkAbility::getFoodLevelCost),
			Codec.INT.fieldOf("required_food_level_to_milk").forGetter(CowMilkAbility::getRequiredMilkFoodLevel),
			AudioVisualEffect.CODEC.optionalFieldOf("drink_milk_self_effect").forGetter(CowMilkAbility::getDrinkMilkSelfEffect),
			AudioVisualEffect.CODEC.optionalFieldOf("milker_entity_effect").forGetter(CowMilkAbility::getMilkerEntityEffect),
			AudioVisualEffect.CODEC.optionalFieldOf("milked_entity_effect").forGetter(CowMilkAbility::getMilkedEntityEffect),
			AudioVisualEffect.CODEC.optionalFieldOf("not_enough_food_feedback_effect").forGetter(CowMilkAbility::getNotEnoughFoodFeedbackEffect))
	.apply(instance, CowMilkAbility::new));
	
	private int foodLevelCost;
	private int requiredMilkFoodLevel;
	private Optional<AudioVisualEffect> drinkMilkSelfEffect;
	
	private Optional<AudioVisualEffect> milkerEntityEffect;
	private Optional<AudioVisualEffect> milkedEntityEffect;
	
	private Optional<AudioVisualEffect> notEnoughFoodFeedbackEffect;
		
	public CowMilkAbility(int foodLevelCost, int requiredMilkFoodLevel, Optional<AudioVisualEffect> drinkMilkSelfEffect, Optional<AudioVisualEffect> milkerEntityEffect,
			Optional<AudioVisualEffect> milkedEntityEffect, Optional<AudioVisualEffect> notEnoughFoodFeedbackEffect)
	{
		this.foodLevelCost = foodLevelCost;
		this.requiredMilkFoodLevel = requiredMilkFoodLevel;
		this.drinkMilkSelfEffect = drinkMilkSelfEffect;
		this.milkerEntityEffect = milkerEntityEffect;
		this.milkedEntityEffect = milkedEntityEffect;
		this.notEnoughFoodFeedbackEffect = notEnoughFoodFeedbackEffect;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(player.getActiveEffects().size() > 0)
		{
			if(canMilk(player))
			{
				drinkMilkSelfEffect.ifPresent(effect -> effect.playEffect(player));
				
				decrementHungerAndSaturation(player);
				player.removeAllEffects();
			}
			else
			{
				this.notEnoughFoodFeedbackEffect.ifPresent(ave -> ave.playEffect(player));
			}
		}
	}
	
	@SubscribeEvent
	public void onGettingInteractedWith(PlayerInteractEvent.EntityInteract event)
	{
		if(event.getHand() == InteractionHand.MAIN_HAND && !event.getWorld().isClientSide() && isTracked(event.getTarget()) && event.getItemStack().is(Items.BUCKET))
		{
			if(canMilk((Player) event.getTarget()))
			{	
				milkerEntityEffect.ifPresent(effect -> effect.playEffect(event.getEntity()));
				milkedEntityEffect.ifPresent(effect -> effect.playEffect(event.getTarget()));
				
		        ItemStack milkStack = ItemUtils.createFilledResult(event.getItemStack(), event.getPlayer(), Items.MILK_BUCKET.getDefaultInstance());	
		        decrementHungerAndSaturation((Player) event.getTarget());
		        
		        event.getPlayer().setItemInHand(event.getHand(), milkStack);
			}
			else
			{
				this.notEnoughFoodFeedbackEffect.ifPresent(ave -> ave.playEffect(event.getTarget()));
			}
		}
	}
	
	private void decrementHungerAndSaturation(Player player)
	{
		player.getFoodData().setSaturation(0);
		player.getFoodData().setFoodLevel(Math.max(player.getFoodData().getFoodLevel() - foodLevelCost, 0));
	}
	
	private boolean canMilk(Player player)
	{
		return player.getFoodData().getFoodLevel() >= requiredMilkFoodLevel;
	}

	public int getFoodLevelCost()
	{
		return foodLevelCost;
	}
	
	public int getRequiredMilkFoodLevel()
	{
		return requiredMilkFoodLevel;
	}

	public Optional<AudioVisualEffect> getDrinkMilkSelfEffect()
	{
		return drinkMilkSelfEffect;
	}

	public Optional<AudioVisualEffect> getMilkerEntityEffect()
	{
		return milkerEntityEffect;
	}

	public Optional<AudioVisualEffect> getMilkedEntityEffect()
	{
		return milkedEntityEffect;
	}
	
	public Optional<AudioVisualEffect> getNotEnoughFoodFeedbackEffect()
	{
		return notEnoughFoodFeedbackEffect;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
