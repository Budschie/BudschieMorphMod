package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiFunction;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

public class ProjectileShootingAbility extends Ability
{
	private BiFunction<PlayerEntity, Vector3d, Entity> projectileSupplier;
	private int stun;
	private int lastTime = 0;
	private HashMap<UUID, Integer> delayHashMap = new HashMap<>();
	
	public ProjectileShootingAbility(BiFunction<PlayerEntity, Vector3d, Entity> projectileSupplier, int stun)
	{
		this.projectileSupplier = projectileSupplier;
		this.stun = stun;
		MinecraftForge.EVENT_BUS.register(this);
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
		if((ServerSetup.server.getTickCounter() - delayHashMap.getOrDefault(player.getUniqueID(), 0)) > stun)
		{
			Entity createdEntity = projectileSupplier.apply(player, player.getForward());
			createdEntity.setPosition(player.getPosX(), player.getPosY() + player.getEyeHeight(), player.getPosZ());
			
			player.world.addEntity(createdEntity);
			
			delayHashMap.put(player.getUniqueID(), ServerSetup.server.getTickCounter());
		}		
	}
	
	@SubscribeEvent
	public void onServerStopped(FMLServerStoppingEvent event)
	{
		delayHashMap.clear();
		System.out.println("Clearing projectile delays...");
	}
}
