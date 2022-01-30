package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CowMilkAbility extends AbstractEventAbility
{
	public static final Codec<CowMilkAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("food_level_cost").forGetter(CowMilkAbility::getFoodLevelCost),
			Codec.INT.fieldOf("required_food_level_to_milk").forGetter(CowMilkAbility::getRequiredMilkFoodLevel),
			AudioVisualEffect.CODEC.optionalFieldOf("drink_milk_self_effect").forGetter(CowMilkAbility::getDrinkMilkSelfEffect),
			AudioVisualEffect.CODEC.optionalFieldOf("milker_entity_effect").forGetter(CowMilkAbility::getMilkerEntityEffect),
			AudioVisualEffect.CODEC.optionalFieldOf("milked_entity_effect").forGetter(CowMilkAbility::getMilkedEntityEffect))
	.apply(instance, CowMilkAbility::new));
	
	private int foodLevelCost;
	private int requiredMilkFoodLevel;
	private Optional<AudioVisualEffect> drinkMilkSelfEffect;
	
	private Optional<AudioVisualEffect> milkerEntityEffect;
	private Optional<AudioVisualEffect> milkedEntityEffect;
		
	public CowMilkAbility(int foodLevelCost, int requiredMilkFoodLevel, Optional<AudioVisualEffect> drinkMilkSelfEffect, Optional<AudioVisualEffect> milkerEntityEffect,
			Optional<AudioVisualEffect> milkedEntityEffect)
	{
		this.foodLevelCost = foodLevelCost;
		this.requiredMilkFoodLevel = requiredMilkFoodLevel;
		this.drinkMilkSelfEffect = drinkMilkSelfEffect;
		this.milkerEntityEffect = milkerEntityEffect;
		this.milkedEntityEffect = milkedEntityEffect;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(canMilk(player) && player.getActiveEffects().size() > 0)
		{
			drinkMilkSelfEffect.ifPresent(effect -> effect.playEffect(player));
			
			decrementHungerAndSaturation(player);
			player.removeAllEffects();
		}
	}
	
	@SubscribeEvent
	public void onGettingInteractedWith(PlayerInteractEvent.EntityInteract event)
	{
		if(event.getHand() == InteractionHand.MAIN_HAND && !event.getWorld().isClientSide() && isTracked(event.getTarget()) && event.getItemStack().is(Items.BUCKET) && canMilk((Player) event.getTarget()))
		{
			milkerEntityEffect.ifPresent(effect -> effect.playEffect(event.getEntity()));
			milkedEntityEffect.ifPresent(effect -> effect.playEffect(event.getTarget()));
			
	        ItemStack milkStack = ItemUtils.createFilledResult(event.getItemStack(), event.getPlayer(), Items.MILK_BUCKET.getDefaultInstance());	
	        decrementHungerAndSaturation((Player) event.getTarget());
	        
	        event.getPlayer().setItemInHand(event.getHand(), milkStack);
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
}
