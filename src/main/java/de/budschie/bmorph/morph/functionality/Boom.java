package de.budschie.bmorph.morph.functionality;

import java.util.Random;

import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.EntityExplosionContext;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.World;

public class Boom extends Ability
{

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
		player.attackEntityFrom(DamageSource.causeExplosionDamage(player), 69420);
		player.world.createExplosion(player, DamageSource.causeExplosionDamage(player), new EntityExplosionContext(player), player.getPosX(), player.getPosY(), player.getPosZ(), 7, false, Mode.BREAK);
		
		Random rand = new Random();
		
		World playerWorld = player.world;
		
		for(int i = 0; i < 100; i++)
		{
			playerWorld.playSound(null, player.getPosition().add(rand.nextInt(21) - 10, rand.nextInt(21) - 10, rand.nextInt(21) - 10), rand.nextBoolean() ? SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE : SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 20, (rand.nextFloat() - .6f) + 1);
		}
	}
}
