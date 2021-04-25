package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.SquidBoost;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

public class SquidBoostAbility extends StunAbility
{
	public SquidBoostAbility()
	{
		super(50);
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
			//player.world.addParticle(ParticleTypes.SQUID_INK, true, player.getPosX(), player.getPosY(), player.getPosZ(), .1f, .1f, .1f);
			
			((ServerWorld)player.world).spawnParticle(ParticleTypes.SQUID_INK, player.getPosX(), player.getPosY(), player.getPosZ(), 300, .4f, .4f, .4f, 0);
			
			player.world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(player.getPosition().add(-5, -5, -5), player.getPosition().add(5, 5, 5))).forEach(entity ->
			{
				if(entity instanceof LivingEntity)
					((LivingEntity)entity).addPotionEffect(new EffectInstance(Effects.BLINDNESS, 120, 5, false, false, false));
			});			
			player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_SQUID_SQUIRT, SoundCategory.NEUTRAL, 10, 1);
			stun(player.getUniqueID());
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.DIMENSION.with(() -> player.world.getDimensionKey()), new SquidBoost.SquidBoostPacket(2.5f, player.getUniqueID()));
		}		
	}
}
