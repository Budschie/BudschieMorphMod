package de.budschie.bmorph.render_handler;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

public class WitherSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof WitherBoss;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		WitherBoss boss = (WitherBoss) morphEntity;
		
		for(int i = 0; i < 2; i++)
		{
			// Fancy formatting
			boss.xRotHeads[i]  = player.getXRot();
			boss.yRotHeads[i]  = player.getYRot();
			boss.xRotOHeads[i] = player.xRotO;
			boss.yRotOHeads[i] = player.yRotO;
		}
		
		for(int headIndex = 0; headIndex < 3; ++headIndex)
		{
			double headX = boss.getHeadX(headIndex);
			double headY = boss.getHeadY(headIndex);
			double headZ = boss.getHeadZ(headIndex);
			boss.level.addParticle(ParticleTypes.SMOKE, headX + boss.getRandom().nextGaussian() * 0.3f, headY + boss.getRandom().nextGaussian() * 0.3f,
					headZ + boss.getRandom().nextGaussian() * 0.3f, 0.0d, 0.0d, 0.0d);
		}
	}
}
