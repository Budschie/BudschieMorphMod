package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.EntityExplosionContext;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;

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
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!player.isCreative() && player.ticksExisted > 80)
		{
			if(isInstantKill())
			{
				player.attackEntityFrom(DamageSource.causeExplosionDamage(player), 69420);
			}
			
			player.world.createExplosion(player, DamageSource.causeExplosionDamage(player), new EntityExplosionContext(player), player.getPosX(), player.getPosY(), player.getPosZ(), explosionSize, causesFire, Mode.BREAK);
			
			Random rand = new Random();
			
			World playerWorld = player.world;
			
			for(int i = 0; i < 2; i++)
			{
				playerWorld.playSound(null, player.getPosition().add(rand.nextInt(21) - 10, rand.nextInt(21) - 10, rand.nextInt(21) - 10), rand.nextBoolean() ? SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE : SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 20, (rand.nextFloat() - .6f) + 1);
			}
		}
	}
}
