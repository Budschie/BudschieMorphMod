package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.Level;

public class Boom extends Ability
{
	private boolean instantKill;
	private float explosionSize;
	private boolean causesFire;
	
	public static final Codec<Boom> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("instant_kill", true).forGetter(Boom::isInstantKill),
			Codec.BOOL.optionalFieldOf("causes_fire", false).forGetter(Boom::causesFire),
			Codec.FLOAT.optionalFieldOf("explosion_size", 7.0f).forGetter(Boom::getExplosionSize)
			).apply(instance, Boom::new));
		
	public Boom(boolean instantKill, boolean causesFire, float explosionSize)
	{
		this.instantKill = instantKill;
		this.causesFire = causesFire;
		this.explosionSize = explosionSize;
	}
	
	public float getExplosionSize()
	{
		return explosionSize;
	}
	
	public boolean causesFire()
	{
		return causesFire;
	}
	
	public boolean isInstantKill()
	{
		return instantKill;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!player.isCreative() && player.tickCount > 80)
		{
			if(isInstantKill())
			{
				player.hurt(DamageSource.explosion(player), 69420);
			}
			
			player.level.explode(player, DamageSource.explosion(player), new EntityBasedExplosionDamageCalculator(player), player.getX(), player.getY(), player.getZ(), explosionSize, causesFire, BlockInteraction.BREAK);
			
			Random rand = new Random();
			
			Level playerWorld = player.level;
			
			for(int i = 0; i < 2; i++)
			{
				playerWorld.playSound(null, player.blockPosition().offset(rand.nextInt(21) - 10, rand.nextInt(21) - 10, rand.nextInt(21) - 10), rand.nextBoolean() ? SoundEvents.DRAGON_FIREBALL_EXPLODE : SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 20, (rand.nextFloat() - .6f) + 1);
			}
		}
	}
}
