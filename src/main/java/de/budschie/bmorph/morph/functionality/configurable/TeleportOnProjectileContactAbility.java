package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TeleportOnProjectileContactAbility extends Ability
{
	public static final Codec<TeleportOnProjectileContactAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("max_tries", 64).forGetter(TeleportOnProjectileContactAbility::getMaxTries),
			ModCodecs.VECTOR_3D_I.optionalFieldOf("search_radius", new Vec3i(32, 32, 32)).forGetter(TeleportOnProjectileContactAbility::getSearchRadius),
			AudioVisualEffect.CODEC.optionalFieldOf("teleportation_effect").forGetter(TeleportOnProjectileContactAbility::getTeleportationEffect)
			).apply(instance, TeleportOnProjectileContactAbility::new));
	
	private int maxTries;
	private Vec3i searchRadius;
	private Optional<AudioVisualEffect> teleportationEffect;
	
	public TeleportOnProjectileContactAbility(int maxTries, Vec3i searchRadius, Optional<AudioVisualEffect> teleportationEffect)
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
		
		if(event.getEntity().level.isClientSide())
			return;
		
		if(event.getSource().isProjectile())
		{
			event.setCanceled(true);
			
			BlockPos foundBlock = null;
			
			algorithm:
			for(int i = 0; i < maxTries; i++)
			{
				int x = (int) (event.getEntityLiving().getRandom().nextInt(searchRadius.getX() * 2) - searchRadius.getX() + event.getEntity().getX());
				int y = (int) (event.getEntityLiving().getRandom().nextInt(searchRadius.getY() * 2) - searchRadius.getY() + event.getEntity().getY());
				int z = (int) (event.getEntityLiving().getRandom().nextInt(searchRadius.getZ() * 2) - searchRadius.getZ() + event.getEntity().getZ());
				
				foundBlock = new BlockPos(x, y, z);
				
				// if(event.getEntity().level.getBlock)
				if(event.getEntity().level.isLoaded(foundBlock))
				{
					BlockState blockAtPos = event.getEntity().level.getBlockState(foundBlock);
					
					if(!blockAtPos.getMaterial().blocksMotion() && !blockAtPos.getFluidState().is(FluidTags.WATER))
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

	public Vec3i getSearchRadius()
	{
		return searchRadius;
	}

	public Optional<AudioVisualEffect> getTeleportationEffect()
	{
		return teleportationEffect;
	}
}
