package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TeleportOnProjectileContactAbility extends Ability
{
	public static final Codec<TeleportOnProjectileContactAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("max_tries", 64).forGetter(TeleportOnProjectileContactAbility::getMaxTries),
			Codec.INT.optionalFieldOf("search_radius", 32).forGetter(TeleportOnProjectileContactAbility::getSearchRadius),
			AudioVisualEffect.CODEC.optionalFieldOf("teleportation_effect").forGetter(TeleportOnProjectileContactAbility::getTeleportationEffect)
			).apply(instance, TeleportOnProjectileContactAbility::new));
	
	private int maxTries;
	private int searchRadius;
	private Optional<AudioVisualEffect> teleportationEffect;
	
	public TeleportOnProjectileContactAbility(int maxTries, int searchRadius, Optional<AudioVisualEffect> teleportationEffect)
	{
		this.maxTries = maxTries;
		this.searchRadius = searchRadius;
		this.teleportationEffect = teleportationEffect;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	@SubscribeEvent
	public void onLivingEntityDamaged(LivingAttackEvent event)
	{
		if(!isTracked(event.getEntity()))
			return;
		
		if(event.getSource().isProjectile())
		{
			event.setCanceled(true);
			
			BlockPos foundBlock = null;
			
			algorithm:
			for(int i = 0; i < maxTries; i++)
			{
				int x = event.getEntityLiving().getRandom().nextInt(searchRadius * 2) - searchRadius;
				int y = event.getEntityLiving().getRandom().nextInt(searchRadius * 2) - searchRadius;
				int z = event.getEntityLiving().getRandom().nextInt(searchRadius * 2) - searchRadius;
				
				foundBlock = new BlockPos(x, y, z);
				
				// if(event.getEntity().level.getBlock)
				if(event.getEntity().level.isLoaded(foundBlock))
				{
					BlockState blockAtPos = event.getEntity().level.getBlockState(foundBlock);
					
					if(blockAtPos.getMaterial().blocksMotion() && !blockAtPos.getFluidState().is(FluidTags.WATER))
					{
						if(event.getEntityLiving().randomTeleport(x, y, z, false))
						{
							teleportationEffect.ifPresent(ave -> ave.playEffect(event.getEntity()));
							break algorithm;
						}
					}
				}
			}
		}
	}

	public int getMaxTries()
	{
		return maxTries;
	}

	public int getSearchRadius()
	{
		return searchRadius;
	}

	public Optional<AudioVisualEffect> getTeleportationEffect()
	{
		return teleportationEffect;
	}
}
