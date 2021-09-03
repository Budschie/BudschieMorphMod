package de.budschie.bmorph.events;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.entity.MorphEntity;
import de.budschie.bmorph.json_integration.MorphAbilityManager;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.PlayerMorphEvent;
import de.budschie.bmorph.morph.functionality.AbilityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class Events
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static int AGGRO_TICKS_TO_PASS = 200;
	
	public static final MorphAbilityManager MORPH_ABILITY_MANAGER = new MorphAbilityManager();
	
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
				cap.resolve().get().getCurrentMorph().ifPresent(morph -> cap.resolve().get().setCurrentAbilities(MORPH_ABILITY_MANAGER.getAbilitiesFor(morph)));
				cap.resolve().get().syncWithClients(player);
				
				cap.resolve().get().applyHealthOnPlayer(player);
				cap.resolve().get().applyAbilities(player);
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(player, cap.resolve().get(), cap.resolve().get().getCurrentMorph().orElse(null)));
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerIsBeingLoaded(PlayerEvent.StartTracking event)
	{
		if(event.getTarget() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getTarget();
			MorphUtil.processCap(player, resolved -> resolved.syncWithClient(player, (ServerPlayerEntity) event.getPlayer()));
		}
	}
	
	@SubscribeEvent
	public static void onRegisterReloadResourceLoaders(AddReloadListenerEvent event)
	{
		event.addListener(MORPH_ABILITY_MANAGER);
	}
	
	@SubscribeEvent
	public static void onPlayerStoppedBeingLoaded(PlayerEvent.StopTracking event)
	{
		
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
						boolean shouldMorph = !ConfigManager.INSTANCE.get(BlacklistData.class).isInBlacklist(event.getEntity().getType().getRegistryName());
						
						if(!resolved.getMorphList().contains(morphItem) && shouldMorph)
						{
							MorphEntity morphEntity = new MorphEntity(event.getEntity().world, morphItem);
							morphEntity.setPosition(event.getEntity().getPosX(), event.getEntity().getPosY(), event.getEntity().getPosZ());
							event.getEntity().world.addEntity(morphEntity);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
	{
		if(!event.getEntity().world.isRemote)
		{
			MorphUtil.processCap(event.getPlayer(), resolved ->
			{
				resolved.syncWithClients(event.getPlayer());
			});
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
				else if(event.getSource() == DamageSource.FALL && resolved.hasAbility(AbilityRegistry.NO_FALL_DAMAGE_ABILITY.get()))
					event.setCanceled(true);
			}
		}
		// Check if living is a Mob and therefore "evil"
		else if(event.getSource().getTrueSource() instanceof PlayerEntity && event.getEntityLiving() instanceof IMob && !event.getEntity().world.isRemote)
		{
			PlayerEntity source = (PlayerEntity) event.getSource().getTrueSource();
			
			LazyOptional<IMorphCapability> cap = source.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			aggro(cap.resolve().get(), ServerSetup.server.getGameRules().getInt(BMorphMod.MORPH_AGGRO_DURATION));
		}
	}
	
	@SubscribeEvent
	public static void onChangedPose(PlayerTickEvent event)
	{
//		event.player.setPose(Pose.SLEEPING);
//		event.player.setForcedPose(Pose.SWIMMING);
		
		MorphUtil.processCap(event.player, cap ->
		{
			if(cap.hasAbility(AbilityRegistry.FLY_ABILITY.get()) && event.player.abilities.isFlying)
			{
				event.player.setPose(Pose.STANDING);
				event.player.setForcedPose(null);
			}
			
			if(cap.getCurrentMorph().isPresent())
			{
				if((event.player.getBoundingBox().maxY - event.player.getBoundingBox().minY) < 1 && event.player.getPose() == Pose.SWIMMING && !event.player.isSwimming())
				{
					event.player.setPose(Pose.STANDING);
				}
			}
		});
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
	
	private static void aggro(IMorphCapability capability, int aggroDuration)
	{
		capability.setLastAggroTimestamp(ServerSetup.server.getTickCounter());
		capability.setLastAggroDuration(aggroDuration);
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
					if(!resolved.hasAbility(AbilityRegistry.MOB_ATTACK_ABILITY.get()) && (ServerSetup.server.getTickCounter() - resolved.getLastAggroTimestamp()) > resolved.getLastAggroDuration())
						aggressor.setAttackTarget(null);
					else
					{
						aggro(resolved, ServerSetup.server.getGameRules().getInt(BMorphMod.MORPH_AGGRO_DURATION));
					}
				}
				// Do this so that we can't morph to player, wait the 10 sec, and move back.
				else
					aggro(resolved, ServerSetup.server.getGameRules().getInt(BMorphMod.MORPH_AGGRO_DURATION));
			}
		}
	}
		
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event)
	{
		ServerSetup.server.getPlayerList().getPlayers().forEach(player ->
		{
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				if(resolved.hasAbility(AbilityRegistry.CLIMBING_ABILITY.get()) && player.collidedHorizontally)
					player.setMotion(player.getMotion().add(0, .1f, 0));
				if(resolved.hasAbility(AbilityRegistry.WATER_BREATHING_ABILITY.get()) && player.isInWater())
					player.setAir(14 * 20);
				if(resolved.hasAbility(AbilityRegistry.WATER_DISLIKE_ABILITY.get()) && player.isInWaterRainOrBubbleColumn())
					player.attackEntityFrom(DamageSource.DROWN, 1);
				if(resolved.hasAbility(AbilityRegistry.FLY_ABILITY.get()))
					player.abilities.allowFlying = true;
			}
		});
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onCalculatingAABB(EntityEvent.Size event)
	{
		if(event.getEntity() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity) event.getEntity();
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
				
				IMorphCapability resolved = cap.resolve().get();
				resolved.getCurrentMorph().ifPresent(item ->
				{					
					try
					{
						Entity createdEntity = item.createEntity(event.getEntity().world);
						createdEntity.setPose(event.getPose());
						
						// We do this as we apply our own sneaking logic as I couldn't figure out how to get the multiplier for the eye height... F in the chat plz
						EntitySize newSize = createdEntity.getSize(Pose.STANDING);
						
						if(ShrinkAPIInteractor.getInteractor().isShrunk(player))
						{
							newSize = newSize.scale(1.6f / divisor, 1 / divisor);
						}
						
						if(event.getPose() == Pose.CROUCHING)
							newSize = newSize.scale(1, .85f);
						
						event.setNewSize(newSize, false);
						//event.setNewEyeHeight(createdEntity.getEyeHeightAccess(event.getPose(), newSize));
						event.setNewEyeHeight(newSize.height * 0.85f);
						//event.setNewEyeHeight(player.getEyeHeightAccess(event.getPose(), createdEntity.getSize(event.getPose())));
					}
					catch(NullPointerException ex)
					{
						LOGGER.catching(ex);
						
						if(!player.world.isRemote)
							MorphUtil.morphToClient(Optional.empty(), Optional.empty(), new ArrayList<String>(), player);
						else
						{
							resolved.demorph();
							player.sendMessage(new StringTextComponent(TextFormatting.RED + "Couldn't morph to " + item.getEntityType().getRegistryName().toString() + ". This is a compatability issue. If possible, report this to the mod author on GitHub."), new UUID(0, 0));
						}
					}
				});
			}
		}
	}
}
