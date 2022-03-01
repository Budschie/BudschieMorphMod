package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BurnInSunAbility extends Ability
{
	public static final Codec<BurnInSunAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(Codec.INT.fieldOf("burn_time").forGetter(BurnInSunAbility::getBurnTime),
					Codec.BOOL.optionalFieldOf("ignore_hat", false).forGetter(BurnInSunAbility::isIgnoringHat),
					Codec.INT.optionalFieldOf("max_armor_damage", 2).forGetter(BurnInSunAbility::getMaxArmorDamage)).apply(instance, BurnInSunAbility::new));
	
	private int burnTime;
	private boolean ignoreHat;
	private int maxArmorDamage;
	
	public BurnInSunAbility(int burnTime, boolean ignoreHat, int maxArmorDamage)
	{
		this.burnTime = burnTime;
		this.ignoreHat = ignoreHat;
		this.maxArmorDamage = maxArmorDamage;
	}
	
	public int getBurnTime()
	{
		return burnTime;
	}
	
	public boolean isIgnoringHat()
	{
		return ignoreHat;
	}
	
	public int getMaxArmorDamage()
	{
		return maxArmorDamage;
	}
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent event)
	{
		if(isTracked(event.player) && !event.player.isInvulnerable() && !event.player.getCommandSenderWorld().isClientSide)
		{
			// Check if entity shall burn
			if (event.player.level.isDay() && !event.player.level.isClientSide)
			{
				// Taken from Mob class and slightly changed
				float currentBrightness = event.player.getBrightness();
				BlockPos blockpos = new BlockPos(event.player.getEyePosition());
				
				boolean shallBeImmuneToFire = event.player.isInWaterRainOrBubble() || event.player.isInPowderSnow || event.player.wasInPowderSnow;
				if (currentBrightness > 0.5F && event.player.getRandom().nextFloat() * 30 < (currentBrightness - 0.4f) * 2.0f && !shallBeImmuneToFire
						&& event.player.level.canSeeSky(blockpos))
				{
					ItemStack hatItem = event.player.getItemBySlot(EquipmentSlot.HEAD);
					
					// Check if we shall account for the hat. If this is the case, damage the head.
					if(!ignoreHat && !hatItem.isEmpty())
					{
						// TODO: This could *maybe* cause a bug where the hat doesn't get destroyed.
						hatItem.setDamageValue(hatItem.getDamageValue() + event.player.getRandom().nextInt(maxArmorDamage));
						
						if (hatItem.getDamageValue() >= hatItem.getMaxDamage())
						{
							event.player.broadcastBreakEvent(EquipmentSlot.HEAD);
							event.player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
						}
						
						return;
					}
					
					event.player.setSecondsOnFire(burnTime);
				}
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
