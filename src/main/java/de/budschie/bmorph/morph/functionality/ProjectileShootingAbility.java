package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class ProjectileShootingAbility extends StunAbility
{
	private BiFunction<PlayerEntity, Vector3d, Entity> projectileSupplier;
	
	public ProjectileShootingAbility(BiFunction<PlayerEntity, Vector3d, Entity> projectileSupplier, int stun)
	{
		super(stun);
		this.projectileSupplier = projectileSupplier;
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
			Entity createdEntity = projectileSupplier.apply(player, Vector3d.fromPitchYaw(player.getPitchYaw()));
			createdEntity.setPosition(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
			player.world.addEntity(createdEntity);
			stun(player.getUniqueID());
		}		
	}
}
