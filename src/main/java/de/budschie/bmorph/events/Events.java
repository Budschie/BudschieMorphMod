package de.budschie.bmorph.events;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.entity.MorphEntity;
import de.budschie.bmorph.gui.MorphGuiHandler;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.PlayerMorphEvent;
import de.budschie.bmorph.morph.PlayerMorphEvent.Server.Post;
import de.budschie.bmorph.morph.functionality.AbilityLookupTableHandler;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.PacketDistributor;

@EventBusSubscriber
public class Events
{
	@SubscribeEvent
	public static void onPlayerJoined(PlayerLoggedInEvent event)
	{
		if(!event.getEntity().world.isRemote)
		{
			PlayerEntity player = event.getPlayer();
			
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Pre(player, cap.resolve().get(), cap.resolve().get().getCurrentMorph().orElse(null)));
				
//				ServerSetup.server.getPlayerList().getPlayers().forEach(serverPlayer -> cap.resolve().get().syncWithClient(event.getPlayer(), serverPlayer));
//				ServerSetup.server.getPlayerList().getPlayers().forEach(serverPlayer -> cap.resolve().get().syncWithClient(serverPlayer, (ServerPlayerEntity) event.getPlayer()));
				cap.resolve().get().getCurrentMorph().ifPresent(morph -> cap.resolve().get().setCurrentAbilities(AbilityLookupTableHandler.getAbilitiesFor(morph)));
				cap.resolve().get().syncWithClients(player);
				
				cap.resolve().get().applyHealthOnPlayer(player);
				cap.resolve().get().applyAbilities(player);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(player, cap.resolve().get(), cap.resolve().get().getCurrentMorph().orElse(null)));
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerKilledLivingEntity(LivingDeathEvent event)
	{
		if(!event.getEntity().world.isRemote)
		{
			if(event.getSource().getTrueSource() instanceof PlayerEntity)
			{
				PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
				
				LazyOptional<IMorphCapability> playerMorph = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(playerMorph.isPresent())
				{
					MorphItem morphItem = MorphManagerHandlers.createMorphFromDeadEntity(event.getEntity());
					
					if(morphItem != null)
					{
						IMorphCapability resolved = playerMorph.resolve().get();
						
						if(!resolved.getMorphList().contains(morphItem))
						{
							MorphEntity morphEntity = new MorphEntity(event.getEntity().world, morphItem);
							morphEntity.forceSetPosition(event.getEntity().getPosX(), event.getEntity().getPosY(), event.getEntity().getPosZ());
							event.getEntity().world.addEntity(morphEntity);
							
							System.out.println("Spawned entity!");
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onClonePlayer(PlayerEvent.Clone event)
	{
		// TODO: This may cause a crash under certain circumstances, so I should maybe replace this code!
		// I've tested it and it doesnt cause a crash. That's good.
		if(!(event.isWasDeath() && !ServerSetup.server.getGameRules().getBoolean(BMorphMod.KEEP_MORPH_INVENTORY)))
		{
			LazyOptional<IMorphCapability> oldCap = event.getOriginal().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			LazyOptional<IMorphCapability> newCap = event.getPlayer().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(oldCap.isPresent() && newCap.isPresent())
			{
				IMorphCapability oldResolved = oldCap.resolve().get();
				IMorphCapability newResolved = newCap.resolve().get();
				
				newResolved.setMorphList(oldResolved.getMorphList());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Pre(event.getPlayer(), newResolved, newResolved.getCurrentMorph().orElse(null)));
				
				oldResolved.getCurrentMorphIndex().ifPresent(morph -> newResolved.setMorph(morph));
				oldResolved.getCurrentMorphItem().ifPresent(morph -> newResolved.setMorph(morph));
				newResolved.setCurrentAbilities(oldResolved.getCurrentAbilities());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(event.getPlayer(), newResolved, newResolved.getCurrentMorph().orElse(null)));
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawnedEvent(PlayerRespawnEvent event)
	{
		if(!event.getPlayer().world.isRemote && ServerSetup.server.getGameRules().getBoolean(BMorphMod.KEEP_MORPH_INVENTORY))
		{
			LazyOptional<IMorphCapability> cap = event.getPlayer().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.syncWithClients(event.getPlayer());
				resolved.applyHealthOnPlayer(event.getPlayer());
				resolved.applyAbilities(event.getPlayer());
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerDeathEvent(LivingDeathEvent event)
	{
		if(event.getEntityLiving() instanceof PlayerEntity && !event.getEntity().world.isRemote)
		{
			PlayerEntity player = (PlayerEntity) event.getEntityLiving();
			
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.deapplyAbilities(player);
				
				if(!ServerSetup.server.getGameRules().getBoolean(BMorphMod.KEEP_MORPH_INVENTORY))
				{
					for(MorphItem item : resolved.getMorphList().getMorphArrayList())
					{
						MorphEntity morphEntity = new MorphEntity(player.world, item);
						morphEntity.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
						player.world.addEntity(morphEntity);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTakingDamage(LivingDamageEvent event)
	{
		if(event.getEntityLiving() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				if(event.getSource().isFireDamage() && resolved.hasAbility(AbilityRegistry.NO_FIRE_DAMAGE_ABILITY.get()))
					event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onMorphedClient(PlayerMorphEvent.Client.Post event)
	{
		event.getPlayer().recalculateSize();
	}
	
	@SubscribeEvent
	public static void onMorphedServer(PlayerMorphEvent.Server.Post event)
	{
		event.getPlayer().recalculateSize();
	}
	
	@SubscribeEvent
	public static void onTargetBeingSet(LivingSetAttackTargetEvent event)
	{
		if(event.getEntityLiving() instanceof MobEntity && event.getTarget() instanceof PlayerEntity && event.getTarget() != event.getEntityLiving().getRevengeTarget())
		{
			PlayerEntity player = (PlayerEntity) event.getTarget();
			MobEntity aggressor = (MobEntity) event.getEntityLiving();
			
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				if(resolved.getCurrentMorph().isPresent())
				{
					if(!resolved.hasAbility(AbilityRegistry.MOB_ATTACK_ABILITY.get()))
						aggressor.setAttackTarget(null);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onCalculatingAABB(EntityEvent.Size event)
	{
		if(event.getEntity() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getEntity();
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				resolved.getCurrentMorph().ifPresent(item ->
				{					
					Entity createdEntity = item.createEntity(event.getEntity().world);
					createdEntity.setPose(event.getPose());
					
					// We do this as we apply our own sneaking logic as I couldn't figure out how to get the multiplier for the eye height... F in the chat plz
					EntitySize newSize = createdEntity.getSize(Pose.STANDING);
					
					if(event.getPose() == Pose.CROUCHING)
						newSize = newSize.scale(1, .85f);
					
					event.setNewSize(newSize, false);
					event.setNewEyeHeight(createdEntity.getEyeHeightAccess(event.getPose(), newSize));
					//event.setNewEyeHeight(player.getEyeHeightAccess(event.getPose(), createdEntity.getSize(event.getPose())));
				});
			}
		}
	}
}
