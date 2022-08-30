package de.budschie.bmorph.morph.functionality.configurable;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
			return;
		
		for(UUID playerId : trackedPlayers)
		{
			Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId);

			if(!player.isInvulnerable() && !player.level.isClientSide)
			{
				// Check if entity shall burn
				if (player.level.isDay() && !player.level.isClientSide)
				{
					// Taken from Mob class and slightly changed
					float currentBrightness = player.getBrightness();
					BlockPos blockpos = new BlockPos(player.getEyePosition());
					
					boolean shallBeImmuneToFire = player.isInWaterRainOrBubble() || player.isInPowderSnow || player.wasInPowderSnow;
					if (currentBrightness > 0.5F && player.getRandom().nextFloat() * 30 < (currentBrightness - 0.4f) * 2.0f && !shallBeImmuneToFire
							&& player.level.canSeeSky(blockpos))
					{
						ItemStack hatItem = player.getItemBySlot(EquipmentSlot.HEAD);
						
						// Check if we shall account for the hat. If this is the case, damage the head.
						if(!ignoreHat && !hatItem.isEmpty())
						{
							hatItem.setDamageValue(hatItem.getDamageValue() + player.getRandom().nextInt(maxArmorDamage));
							
							if (hatItem.getDamageValue() >= hatItem.getMaxDamage())
							{
								player.broadcastBreakEvent(EquipmentSlot.HEAD);
								player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
							}
							
							return;
						}
						
						player.setSecondsOnFire(burnTime);
					}
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
