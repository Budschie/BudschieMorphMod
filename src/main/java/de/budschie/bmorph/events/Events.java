package de.budschie.bmorph.events;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.blacklist.BlacklistData;
import de.budschie.bmorph.capabilities.blacklist.ConfigManager;
import de.budschie.bmorph.capabilities.bossbar.BossbarCapabilityInstance;
import de.budschie.bmorph.capabilities.bossbar.IBossbarCapability;
import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.capabilities.custom_riding_data.CustomRidingDataInstance;
import de.budschie.bmorph.capabilities.custom_riding_data.ICustomRidingData;
import de.budschie.bmorph.capabilities.evoker.EvokerSpellCapabilityHandler;
import de.budschie.bmorph.capabilities.evoker.IEvokerSpellCapability;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityHandler;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityInstance;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import de.budschie.bmorph.capabilities.morph_attribute_modifiers.IMorphAttributeModifiers;
import de.budschie.bmorph.capabilities.morph_attribute_modifiers.MorphAttributeModifiersInstance;
import de.budschie.bmorph.capabilities.parrot_dance.IParrotDanceCapability;
import de.budschie.bmorph.capabilities.parrot_dance.ParrotDanceCapabilityHandler;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityHandler;
import de.budschie.bmorph.capabilities.phantom_glide.IGlideCapability;
import de.budschie.bmorph.capabilities.proxy_entity_cap.IProxyEntityCapability;
import de.budschie.bmorph.capabilities.pufferfish.IPufferfishCapability;
import de.budschie.bmorph.capabilities.pufferfish.PufferfishCapabilityHandler;
import de.budschie.bmorph.capabilities.sheep.ISheepCapability;
import de.budschie.bmorph.capabilities.sheep.SheepCapabilityHandler;
import de.budschie.bmorph.capabilities.speed_of_morph_cap.IPlayerUsingSpeedOfMorph;
import de.budschie.bmorph.capabilities.stand_on_fluid.IStandOnFluidCapability;
import de.budschie.bmorph.capabilities.stand_on_fluid.StandOnFluidInstance;
import de.budschie.bmorph.entity.MorphEntity;
import de.budschie.bmorph.json_integration.AbilityConfigurationHandler;
import de.budschie.bmorph.json_integration.DataTransformerHandler;
import de.budschie.bmorph.json_integration.MorphAbilityManager;
import de.budschie.bmorph.json_integration.MorphNBTHandler;
import de.budschie.bmorph.json_integration.VisualMorphDataHandler;
import de.budschie.bmorph.json_integration.ability_groups.AbilityGroups;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.MorphReasonRegistry;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.network.ChangeUsingSpeedOfMorph;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.tags.ModEntityTypeTags;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

@EventBusSubscriber
public class Events
{
	private static final Logger LOGGER = LogManager.getLogger();
	public static int AGGRO_TICKS_TO_PASS = 200;
	
	// This field indicates whether we should resolve the ability names or not
	public static final MorphAbilityManager MORPH_ABILITY_MANAGER = new MorphAbilityManager();
	public static final MorphNBTHandler MORPH_NBT_HANDLER = new MorphNBTHandler();
	public static final AbilityConfigurationHandler ABILITY_CONFIG_HANDLER = new AbilityConfigurationHandler();
	public static final DataTransformerHandler DATA_TRANSFORMER_HANDLER = new DataTransformerHandler();
	public static final VisualMorphDataHandler VISUAL_MORPH_DATA_HANDLER = new VisualMorphDataHandler();
	public static final AbilityGroups ABILITY_GROUP_HANDLER = new AbilityGroups();
	
	@SubscribeEvent
	public static void onAcquiredMorph(AcquiredMorphEvent.Post event)
	{
		BMorphMod.ACQUIRED_MORPH.trigger(event.getMorph(), (ServerPlayer) event.getPlayer());
	}
	
	@SubscribeEvent
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event)
	{
		event.register(IMorphCapability.class);
		event.register(IPufferfishCapability.class);
		event.register(IGuardianBeamCapability.class);
		event.register(IParrotDanceCapability.class);
		event.register(IGlideCapability.class);
		event.register(ISheepCapability.class);
		event.register(IRenderDataCapability.class);
		event.register(IBossbarCapability.class);
		event.register(IStandOnFluidCapability.class);
		event.register(IEvokerSpellCapability.class);
		event.register(IProxyEntityCapability.class);
		event.register(ICustomRidingData.class);
		event.register(IMorphAttributeModifiers.class);
		event.register(IPlayerUsingSpeedOfMorph.class);
	}
	
	// Add additional target selector to iron golem entity
	@SubscribeEvent
	public static void onEntityCreated(EntityJoinWorldEvent event)
	{
		if(event.getWorld().isClientSide())
			return;
		
		if(event.getEntity() instanceof IronGolem ironGolem)
		{
			ironGolem.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(ironGolem, Player.class, 5, false, false, player ->
			{
				IMorphCapability cap = MorphUtil.getCapOrNull((Player) player);

				if(cap != null)
				{
					Optional<MorphItem> morphItem = cap.getCurrentMorph();

					if(morphItem.isPresent())
					{
						MorphItem morphItemResolved = morphItem.get();
						
						// We should probably ignore players when accessing the cache.
						if(morphItemResolved.getEntityType() == EntityType.PLAYER || morphItemResolved.getEntityType() == EntityType.CREEPER)
							return false;
						
						Class<? extends Entity> entityClass = EntityClassByTypeCache.getClassForEntityType(morphItemResolved.getEntityType());
						return Enemy.class.isAssignableFrom(entityClass);
					}
				}

				return false;
			}));
		}
	}
	
	@SubscribeEvent
	public static void onPlayerJoined(PlayerLoggedInEvent event)
	{	
		if(!event.getEntity().level.isClientSide())
		{
			Player player = event.getPlayer();
			
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Pre(player, cap.resolve().get(), cap.resolve().get().getCurrentMorph().orElse(null), MorphReasonRegistry.NONE.get()));
				
//				ServerSetup.server.getPlayerList().getPlayers().forEach(serverPlayer -> cap.resolve().get().syncWithClient(event.getPlayer(), serverPlayer));
//				ServerSetup.server.getPlayerList().getPlayers().forEach(serverPlayer -> cap.resolve().get().syncWithClient(serverPlayer, (ServerPlayerEntity) event.getPlayer()));
				cap.resolve().get().getCurrentMorph().ifPresent(morph -> cap.resolve().get().setCurrentAbilities(morph.getAbilities()));
				cap.resolve().get().syncWithClients();
				cap.resolve().get().applyHealthOnPlayer();
				cap.resolve().get().applyAbilities(null, Arrays.asList());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(player, cap.resolve().get(), cap.resolve().get().getCurrentMorph().orElse(null), MorphReasonRegistry.NONE.get()));				
			}
			
			PufferfishCapabilityHandler.INSTANCE.synchronizeWithClients(player);
			GuardianBeamCapabilityHandler.INSTANCE.synchronizeWithClients(player);
			GlideCapabilityHandler.INSTANCE.synchronizeWithClients(player);
			ParrotDanceCapabilityHandler.INSTANCE.synchronizeWithClients(player);
			SheepCapabilityHandler.INSTANCE.synchronizeWithClients(player);
			EvokerSpellCapabilityHandler.INSTANCE.synchronizeWithClients(player);
			
			showBossbarToEveryoneTrackingPlayer(player);
		}
	}
	
	public static void showBossbarToEveryoneTrackingPlayer(Player player)
	{
		player.getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(bossbarCap ->
		{
			bossbarCap.getBossbar().ifPresent(bossbar ->
			{
				BudschieUtils.getPlayersTrackingEntityAndSelf((ServerPlayer) player).forEach(trackingPlayer -> bossbar.addPlayer(trackingPlayer));
			});
		});
	}
	
	@SubscribeEvent
	public static void onPlayerLeft(PlayerLoggedOutEvent event)
	{
		if(!event.getPlayer().level.isClientSide())
		{
			event.getPlayer().getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(bossbarCap -> bossbarCap.clearBossbar());
		}
	}
	
	@SubscribeEvent
	public static void onEntityLeftWorld(EntityLeaveWorldEvent event)
	{
		if(event.getEntity() instanceof Player player)
		{
			if(player.isRemoved() && player.getRemovalReason() == RemovalReason.UNLOADED_WITH_PLAYER)
			{
				MorphUtil.processCap(player, cap ->
				{
					cap.removePlayerReferences();
				});
			}
			
			if(event.getEntity().getRemovalReason() != RemovalReason.CHANGED_DIMENSION && event.getEntity().level.isClientSide())
				player.getCapability(RenderDataCapabilityProvider.RENDER_CAP).ifPresent(cap -> cap.invalidateCache());
		}
	}
	
	@SubscribeEvent
	public static void onDatapackSyncing(OnDatapackSyncEvent event)
	{
		if(event.getPlayer() == null)
		{
			BMorphMod.DYNAMIC_ABILITY_REGISTRY.syncWithClients();
			BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.syncWithClients();
			BMorphMod.VISUAL_MORPH_DATA.syncWithClients();
			BMorphMod.ABILITY_GROUPS.syncWithClients();
			
			event.getPlayerList().getPlayers().forEach(player ->
			{
				IMorphCapability cap = MorphUtil.getCapOrNull(player);
				
				if(cap != null)
				{
					// We need to force it since we would otherwise not remorph when our current morph item has been disabled
					MorphUtil.morphToServer(cap.getCurrentMorph(), cap.getMorphReason(), player, true);
				}
			});
		}
		else
		{
			BMorphMod.DYNAMIC_ABILITY_REGISTRY.syncWithClient(event.getPlayer());
			BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.syncWithClient(event.getPlayer());
			BMorphMod.VISUAL_MORPH_DATA.syncWithClient(event.getPlayer());
			BMorphMod.ABILITY_GROUPS.syncWithClient(event.getPlayer());
		}
	}
	
	@SubscribeEvent
	public static void onMorphCreatedFromEntity(MorphCreatedFromEntityEvent event)
	{
		// If the killed morph is an ageable mob, use the age cutter data transformer
		if(event.getEntity() instanceof AgeableMob)
		{
			BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY.getEntry(new ResourceLocation(References.MODID, "age_cutter")).transformData(event.getTagIn(), event.getTagOut());
		}
	}
	
	@SubscribeEvent
	public static void onPlayerIsBeingLoaded(PlayerEvent.StartTracking event)
	{
		if(event.getTarget() instanceof Player)
		{
			Player player = (Player) event.getTarget();
			MorphUtil.processCap(player, resolved -> resolved.syncWithClient((ServerPlayer) event.getPlayer()));
			
			PufferfishCapabilityHandler.INSTANCE.synchronizeWithClient(player, (ServerPlayer) event.getPlayer());
			GuardianBeamCapabilityHandler.INSTANCE.synchronizeWithClient(player, (ServerPlayer) event.getPlayer());
			GlideCapabilityHandler.INSTANCE.synchronizeWithClient(player, (ServerPlayer) event.getPlayer());
			ParrotDanceCapabilityHandler.INSTANCE.synchronizeWithClient(player, (ServerPlayer) event.getPlayer());
			SheepCapabilityHandler.INSTANCE.synchronizeWithClient(player, (ServerPlayer) event.getPlayer());
			EvokerSpellCapabilityHandler.INSTANCE.synchronizeWithClient(player, (ServerPlayer) event.getPlayer());
			
			
			event.getTarget().getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(bossbarCap ->
			{
				bossbarCap.getBossbar().ifPresent(bossbar -> bossbar.addPlayer((ServerPlayer) event.getPlayer()));
			});
		}
	}
	
	@SubscribeEvent
	public static void onRegisterReloadResourceLoaders(AddReloadListenerEvent event)
	{
		event.addListener(ABILITY_CONFIG_HANDLER);
		event.addListener(MORPH_ABILITY_MANAGER);
		event.addListener(DATA_TRANSFORMER_HANDLER);
		event.addListener(MORPH_NBT_HANDLER);
		event.addListener(VISUAL_MORPH_DATA_HANDLER);
		event.addListener(ABILITY_GROUP_HANDLER);
	}
	
	@SubscribeEvent
	public static void onPlayerStoppedBeingLoaded(PlayerEvent.StopTracking event)
	{
		event.getPlayer().getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).ifPresent(cap ->
		{
			if(cap.getAttackedEntity().isPresent() && cap.getAttackedEntity().get() == event.getTarget().getId())
			{
				GuardianBeamCapabilityHandler.INSTANCE.unattackServer(event.getPlayer());
			}
		});
		
		if(event.getTarget() instanceof Player player)
		{
			event.getTarget().getCapability(BossbarCapabilityInstance.BOSSBAR_CAP).ifPresent(bossbarCap ->
			{
				bossbarCap.getBossbar().ifPresent(bossbar -> bossbar.removePlayer((ServerPlayer) event.getPlayer()));
			});
		}
	}
	
	private static boolean mayUseTool(Player player)
	{		
		if(!player.level.getGameRules().getBoolean(BMorphMod.ALLOW_MORPH_TOOLS))
		{
			IMorphCapability cap = MorphUtil.getCapOrNull(player);
			
			if(cap != null)
			{
				if(cap.getCurrentMorph().isPresent())
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	// Gamerules are currently not synced, leading to a bit buggy drop times when having the gamerule enabled
	@SubscribeEvent
	public static void onPlayerBreakingBlockCheck(PlayerEvent.HarvestCheck event)
	{
		if(!event.getPlayer().isCreative() && !mayUseTool(event.getPlayer()) && event.getTargetBlock().requiresCorrectToolForDrops())
			event.setCanHarvest(false);
	}
	
	@SubscribeEvent
	public static void onPlayerBreakingBlockSpeed(PlayerEvent.BreakSpeed event)
	{
		if(!event.getPlayer().isCreative() && !mayUseTool(event.getPlayer()) && event.getPlayer().getMainHandItem().getItem() instanceof TieredItem)
			event.setNewSpeed(event.getOriginalSpeed() / ((TieredItem)event.getPlayer().getMainHandItem().getItem()).getTier().getSpeed());
	}
	
	@SubscribeEvent
	public static void onPlayerInteractItem(PlayerInteractEvent.RightClickItem event)
	{
		if(!event.getPlayer().isCreative() && !mayUseTool(event.getPlayer()))
		{
			if(event.getItemStack().getItem() instanceof TieredItem || event.getItemStack().getItem() instanceof ProjectileWeaponItem)
				event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onInteractAtBlock(PlayerInteractEvent.RightClickBlock event)
	{
		if(!event.getPlayer().isCreative() && !mayUseTool(event.getPlayer()) && !(event.getItemStack().getItem() instanceof BlockItem))
		{
			event.setUseItem(Result.DENY);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerKilledLivingEntity(LivingDeathEvent event)
	{		
		if(!event.getEntity().level.isClientSide && event.getEntity().getServer().getGameRules().getBoolean(BMorphMod.DO_MORPH_DROPS))
		{
			if(event.getSource().getEntity() instanceof Player)
			{
				Player player = (Player) event.getSource().getEntity();
				
				boolean selfKill = event.getSource().getEntity() == event.getEntity();
				
				if(!(player instanceof FakePlayer || selfKill))
				{
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
								MorphEntity morphEntity = new MorphEntity(event.getEntity().level, morphItem);
								morphEntity.setPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
								event.getEntity().level.addFreshEntity(morphEntity);
							}
						}
					}
				}				
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event)
	{
		if(!event.getEntity().level.isClientSide)
		{
			MorphUtil.processCap(event.getPlayer(), resolved ->
			{
				resolved.syncWithClients();
			});
		}
	}
	
	@SubscribeEvent
	public static void onClonePlayer(PlayerEvent.Clone event)
	{
		if(!(event.isWasDeath() && !event.getEntity().getServer().getGameRules().getBoolean(BMorphMod.KEEP_MORPH_INVENTORY)))
		{
			event.getOriginal().reviveCaps();
			
			Optional<IMorphCapability> oldCap = event.getOriginal().getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve();
			Optional<IMorphCapability> newCap = event.getPlayer().getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve();
			
			event.getOriginal().invalidateCaps();
			
			if(oldCap.isPresent() && newCap.isPresent())
			{
				IMorphCapability oldResolved = oldCap.get();
				IMorphCapability newResolved = newCap.get();
				
				newResolved.setMorphList(oldResolved.getMorphList());
				newResolved.setFavouriteList(oldResolved.getFavouriteList());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Pre(event.getPlayer(), newResolved, newResolved.getCurrentMorph().orElse(null), MorphReasonRegistry.NONE.get()));
				
				oldResolved.getCurrentMorph().ifPresentOrElse(morphItem -> newResolved.setMorph(morphItem, oldResolved.getMorphReason()), () -> newResolved.demorph(oldResolved.getMorphReason()));
				newResolved.setCurrentAbilities(oldResolved.getCurrentAbilities());
				
				MinecraftForge.EVENT_BUS.post(new PlayerMorphEvent.Server.Post(event.getPlayer(), newResolved, newResolved.getCurrentMorph().orElse(null), MorphReasonRegistry.NONE.get()));
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawnedEvent(PlayerRespawnEvent event)
	{
		if(!event.getPlayer().level.isClientSide && event.getPlayer().getServer().getGameRules().getBoolean(BMorphMod.KEEP_MORPH_INVENTORY))
		{
			LazyOptional<IMorphCapability> cap = event.getPlayer().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				resolved.syncWithClients();
				resolved.applyHealthOnPlayer();
				resolved.applyAbilities(null, Arrays.asList());
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerDeathEvent(LivingDeathEvent event)
	{
		if(event.getEntityLiving() instanceof Player player)
		{
			if(event.getEntityLiving().level.isClientSide())
			{
			}
			else
			{
				LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
				
				if(cap.isPresent())
				{
					IMorphCapability resolved = cap.resolve().get();
					
					resolved.deapplyAbilities(null, Arrays.asList());
					
					if(!event.getEntity().getServer().getGameRules().getBoolean(BMorphMod.KEEP_MORPH_INVENTORY))
					{
						for(MorphItem item : resolved.getMorphList())
						{
							MorphEntity morphEntity = new MorphEntity(player.level, item);
							morphEntity.setPos(player.getX(), player.getY(), player.getZ());
							player.level.addFreshEntity(morphEntity);
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerHurtEvent(LivingAttackEvent event)
	{
		if(event.getEntityLiving() instanceof Player player)
		{
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				// We shouldn't take any damage from slimes if they are not aggroed on us
				if(resolved.getCurrentMorph().isPresent() && event.getSource().getEntity() != null && event.getSource().getEntity() instanceof Slime slime
						&& slime.getTarget() != player && (event.getEntity().getServer().getTickCount() - resolved.getLastAggroTimestamp()) > resolved.getLastAggroDuration())
				{
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerTakingDamage(LivingDamageEvent event)
	{
		// Check if living is a Mob and therefore "evil"
		if(event.getSource().getEntity() instanceof Player && event.getEntityLiving() instanceof Enemy && !event.getEntity().level.isClientSide)
		{
			Player source = (Player) event.getSource().getEntity();
			
			LazyOptional<IMorphCapability> cap = source.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			aggro(cap.resolve().get(), event.getEntity().getServer().getGameRules().getInt(BMorphMod.MORPH_AGGRO_DURATION));
		}
	}
	
	@SubscribeEvent
	public static void onChangedPose(PlayerTickEvent event)
	{
//		event.player.setPose(Pose.SLEEPING);
//		event.player.setForcedPose(Pose.SWIMMING);
		
		if(event.phase == Phase.END)
		{
			MorphUtil.processCap(event.player, cap ->
			{
				if(cap.getCurrentMorph().isPresent())
				{
					if((event.player.getBoundingBox().maxY - event.player.getBoundingBox().minY) < 1 && event.player.getPose() == Pose.SWIMMING && !event.player.isSwimming())
					{
						event.player.setPose(Pose.STANDING);
					}
				}
			});
			
			event.player.getCapability(StandOnFluidInstance.STAND_ON_FLUID_CAP).ifPresent(cap ->
			{
		         FluidState state = event.player.level.getFluidState(new BlockPos(event.player.position().add(0, 0.5, 0)));
		         
				if (cap.containsFluid(state.getType()) && event.player.isAffectedByFluids())
				{
					event.player.setDeltaMovement(event.player.getDeltaMovement().scale(0.5D).add(0.0D, 0.15D, 0.0D));
				}
			});
		}
	}
	
	@SubscribeEvent
	public static void onMorphedClient(PlayerMorphEvent.Client.Post event)
	{
		event.getPlayer().refreshDimensions();
		
		handleCustomRidingOffset(event.getPlayer(), event.getAboutToMorphTo());
	}
	
	public static boolean mayCopySpeed(LivingEntity entity)
	{
		return entity.getLevel().getGameRules().getBoolean(BMorphMod.INHERIT_MORPH_SPEED) && !(entity.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) == 0.7f && !ForgeRegistries.ENTITIES.tags().getTag(ModEntityTypeTags.FORCE_SPEED_COPY).contains(entity.getType())) &&
				!ForgeRegistries.ENTITIES.tags().getTag(ModEntityTypeTags.PROHIBIT_SPEED_COPY).contains(entity.getType());
	}
	
	@SubscribeEvent
	public static void onMorphedServer(PlayerMorphEvent.Server.Post event)
	{
		event.getPlayer().refreshDimensions();
		
		IMorphCapability cap = MorphUtil.getCapOrNull(event.getPlayer());
		IMorphAttributeModifiers attribCap = event.getPlayer().getCapability(MorphAttributeModifiersInstance.MORPH_ATTRIBUTE_MODIFIERS_CAP).orElse(null);
		
		if(cap != null && attribCap != null)
		{			
			boolean copySpeed = false;
			boolean isDefault = true;
			
			Optional<AttributeMap> attributesToCopy = Optional.empty();
			
			if(event.getAboutToMorphTo() != null && cap.getCurrentMorphEntity().isPresent())
			{
				Entity toMorphTo = cap.getCurrentMorphEntity().get();
				
				if(toMorphTo instanceof LivingEntity living)
				{
					// living.getAttributes().attributes.forEach((attribute, instance) -> event.getPlayer().getAttribute(attribute).setBaseValue(instance.getBaseValue()));
					attributesToCopy = Optional.of(living.getAttributes());
					copySpeed = mayCopySpeed(living);
					isDefault = false;
				}
			}
			
			// Bug here
			AttributeMap toCopyFromAttributeMap = attributesToCopy.orElseGet(() -> new AttributeMap(DefaultAttributes.getSupplier(EntityType.PLAYER)));
			
			for(Attribute playerAttributes : DefaultAttributes.getSupplier(EntityType.PLAYER).instances.keySet())
			{
				if(playerAttributes == Attributes.MOVEMENT_SPEED)
				{
					if(!copySpeed && !isDefault)
					{
						event.getPlayer().getAttribute(playerAttributes).setBaseValue(DefaultAttributes.getSupplier(EntityType.PLAYER).getBaseValue(Attributes.MOVEMENT_SPEED));
						continue;
					}
				}
				
				// Copy over attribute modifiers as well
				if(toCopyFromAttributeMap.hasAttribute(playerAttributes))
				{
					// Check if the attribute instance for the speed is the same as the attrib instance of the supplier of a living entity.
					
					event.getPlayer().getAttribute(playerAttributes).setBaseValue(toCopyFromAttributeMap.getBaseValue(playerAttributes));
					
					for(AttributeModifier modifier : toCopyFromAttributeMap.getInstance(playerAttributes).getModifiers())
					{
						AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation());
						event.getPlayer().getAttribute(playerAttributes).addTransientModifier(mod);
						attribCap.addAttributeModifier(playerAttributes, mod);
					}
				}
				// event.getPlayer().getAttribute(aPair.getKey()).setBaseValue();
			}
			
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getPlayer()), new ChangeUsingSpeedOfMorph.ChangeUsingSpeedOfMorphPacket(copySpeed));
		}
		else
		{
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)event.getPlayer()), new ChangeUsingSpeedOfMorph.ChangeUsingSpeedOfMorphPacket(false));
		}
		
		if(event.getAboutToMorphTo() == null)
			return;
		
		BMorphMod.MORPHED_INTO.trigger(event.getAboutToMorphTo(), (ServerPlayer) event.getPlayer());
		
		handleCustomRidingOffset(event.getPlayer(), event.getAboutToMorphTo());
	}
	
	private static void handleCustomRidingOffset(Player player, MorphItem aboutToMorphTo)
	{
		LazyOptional<ICustomRidingData> customRidingOffsetCap = player.getCapability(CustomRidingDataInstance.CUSTOM_RIDING_DATA_CAP);
		
		if(customRidingOffsetCap.isPresent())
		{
			ICustomRidingData resolved = customRidingOffsetCap.resolve().get();
			
			if(aboutToMorphTo == null)
			{
				resolved.setCustomRidingOffset(Optional.empty());
			}
			else
			{
				Entity entityInstance = aboutToMorphTo.createEntity(player.getLevel());
				resolved.setCustomRidingOffset(Optional.of(entityInstance.getMyRidingOffset()));
			}
		}
	}
	
	@SubscribeEvent
	public static void onMorphingServer(PlayerMorphEvent.Server.Pre event)
	{		
		if(event.getAboutToMorphTo() != null && event.getAboutToMorphTo().isDisabled())
		{
			event.setCanceled(true);
			return;
		}
		
		// If the morph reason is that the client requested to morph via the UI, perform a space check
		if(event.getMorphReason() == MorphReasonRegistry.MORPHED_BY_UI.get() && !event.getPlayer().getLevel().getGameRules().getBoolean(BMorphMod.SKIP_SPACE_RESTRICTION_CHECK))
		{
			AABB foundShape = null;
			
			if(event.getAboutToMorphTo() == null)
			{
				foundShape = EntityType.PLAYER.getAABB(event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ());
			}
			else
			{
				// Create the entity and set its location. Then, perform a collision test.
				Entity entity = event.getAboutToMorphTo().createEntity(event.getPlayer().getLevel());
				entity.setPos(event.getPlayer().position());
				foundShape = entity.getBoundingBox();
			}
			
			boolean found = false;
			
			for(VoxelShape shape : event.getPlayer().getLevel().getBlockCollisions(event.getPlayer(), foundShape))
			{
				event.setCanceled(true);
				found = true;
				event.getPlayer().sendMessage(new TranslatableComponent("ui.bmorph.not_enough_space").withStyle(ChatFormatting.RED), new UUID(0, 0));
				break;
			}
			
			// We need this weird construct because Eclipse will whine about a non-closed closeable else
			if(found)
			{
				return;
			}
		}
		
		event.getMorphCapability().getCurrentMorph().ifPresent(currentMorph -> BMorphMod.DEMORPHED_FROM.trigger(currentMorph, (ServerPlayer) event.getPlayer()));
		
		IMorphAttributeModifiers attribCap = event.getPlayer().getCapability(MorphAttributeModifiersInstance.MORPH_ATTRIBUTE_MODIFIERS_CAP).orElse(null);
		
		if(attribCap != null)
		{
			attribCap.removeAllAttributesFromPlayer(event.getPlayer());
			// Set default attributes
		}
	}
	
	private static void aggro(IMorphCapability capability, int aggroDuration)
	{
		capability.setLastAggroTimestamp(ServerLifecycleHooks.getCurrentServer().getTickCount());
		capability.setLastAggroDuration(aggroDuration);
	}
	
	@SubscribeEvent
	public static void onTargetBeingSet(LivingChangeTargetEvent event)
	{
		if(event.getEntity().level.isClientSide())
			return;
		
		// This iron golem exception is needed because we don't want players morphed as zombies to sneak around iron golems 
		if(event.getEntityLiving() instanceof Mob && event.getNewTarget() instanceof Player && event.getNewTarget() != event.getEntityLiving().getLastHurtByMob() && !(event.getEntity() instanceof IronGolem || event.getEntity() instanceof EnderMan))
		{
			Player player = (Player) event.getNewTarget();
			
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				IMorphCapability resolved = cap.resolve().get();
				
				if(resolved.getCurrentMorph().isPresent())
				{
					if(!resolved.shouldMobsAttack() && (event.getEntity().getServer().getTickCount() - resolved.getLastAggroTimestamp()) > resolved.getLastAggroDuration())
					{
						event.setCanceled(true);
					}
					else
					{
						aggro(resolved, event.getEntity().getServer().getGameRules().getInt(BMorphMod.MORPH_AGGRO_DURATION));
					}
				}
				// Do this so that we can't morph to player, wait the 10 sec, and move back.
				else
				{
					aggro(resolved, event.getEntity().getServer().getGameRules().getInt(BMorphMod.MORPH_AGGRO_DURATION));
				}
			}
		}		
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onCalculatingAABB(EntityEvent.Size event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(cap.isPresent())
			{
				float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
				
				IMorphCapability resolved = cap.resolve().get();
				resolved.getCurrentMorph().ifPresent(item ->
				{					
					try
					{
						Entity createdEntity = item.createEntity(event.getEntity().level);
						createdEntity.setPose(event.getPose());
						
						// We do this as we apply our own sneaking logic as I couldn't figure out how to get the multiplier for the eye height... F in the chat plz
						EntityDimensions newSize = createdEntity.getDimensions(Pose.STANDING);
						
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
						
						if(!player.level.isClientSide)
							MorphUtil.morphToServer(Optional.empty(), MorphReasonRegistry.MORPHED_BY_ERROR.get(), player);
						else
						{
							resolved.demorph(MorphReasonRegistry.MORPHED_BY_ERROR.get());
							player.sendMessage(new TextComponent(ChatFormatting.RED + "Couldn't morph to " + item.getEntityType().getRegistryName().toString() + ". This is a compatability issue. If possible, report this to the mod author on GitHub."), new UUID(0, 0));
						}
					}
				});
			}
		}
	}
}
