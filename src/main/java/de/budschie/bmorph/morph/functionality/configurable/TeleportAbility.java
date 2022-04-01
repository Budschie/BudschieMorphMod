package de.budschie.bmorph.morph.functionality.configurable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

public class TeleportAbility extends StunAbility
{
	public static final Codec<TeleportAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(Codec.INT.fieldOf("stun").forGetter(TeleportAbility::getStun)).apply(instance, TeleportAbility::new));
	
	public TeleportAbility(int stun)
	{
		super(stun);
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
			Vec3 from = player.position().add(Vec3.directionFromRotation(player.getRotationVector())).add(0, player.getEyeHeight(), 0);
			Vec3 to = Vec3.directionFromRotation(player.getRotationVector()).multiply(50, 50, 50).add(from);
			
			ClipContext context = new ClipContext(from, to, Block.VISUAL, Fluid.SOURCE_ONLY, null);
			
			BlockHitResult result = player.level.clip(context);
			
			// We are looking for a length of 25 (25^2=625)
			if(result.getType() == HitResult.Type.BLOCK)
			{
				player.level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1, .7f);
				((ServerLevel)player.level).sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY(), player.getZ(), 300, 1, 1, 1, 0);
				player.teleportToWithTicket(result.getLocation().x(), result.getLocation().y() + 1, result.getLocation().z());
				player.level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1, 2f);
				
				// Reset the fall distance to negate all fall damage
				player.fallDistance = 0;
				
				stun(player.getUUID());
			}
		}
	}
}
