package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityHandler;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityInstance;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import de.budschie.bmorph.events.GuardianAbilityStatusUpdateEvent;
import de.budschie.bmorph.events.PlayerMorphEvent;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.configurable.client.GuardianClientAdapter;
import de.budschie.bmorph.morph.functionality.configurable.client.GuardianClientAdapterInstance;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

public class GuardianAbility extends Ability
{
	public static final Codec<GuardianAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("damage").forGetter(GuardianAbility::getDamage),
			Codec.BOOL.optionalFieldOf("may_move", false).forGetter(GuardianAbility::mayMove),
			Codec.BOOL.optionalFieldOf("may_cancel", false).forGetter(GuardianAbility::mayCancel),
			Codec.BOOL.optionalFieldOf("may_xray", false).forGetter(GuardianAbility::mayXray),
			Codec.INT.optionalFieldOf("attack_duration", 80).forGetter(GuardianAbility::getAttackDuration),
			Codec.DOUBLE.fieldOf("max_distance_to_target").forGetter(GuardianAbility::getMaxDistance),
			SoundInstance.CODEC.optionalFieldOf("cancel_ability_sound").forGetter(GuardianAbility::getCancelAbilitySound),
			SoundInstance.CODEC.optionalFieldOf("successful_attack_sound").forGetter(GuardianAbility::getSuccessfullyAttackedSound))
			.apply(instance, GuardianAbility::new));
	
	// Variable indicating the damage that this attack will eventually perform
	private float damage;
	// Boolean telling this ability whether you may move whilst having this ability active
	private boolean mayMove;
	// This boolean is indicating whether you can cancel this ability or not
	private boolean mayCancel;
	// This is a boolean indicating whether this laser beam can shoot through walls or not
	private boolean xray;
	
	private double maxDistance;
	
	private int attackDuration;
	
	private Optional<SoundInstance> cancelAbilitySound;
	private Optional<SoundInstance> successfullyAttackedSound;
	
	private AttributeModifier am = new AttributeModifier(UUID.fromString("6605b0e2-98bf-4406-b7e7-39fa26ca9895"), "no movement guardian", -1, Operation.MULTIPLY_TOTAL);
	
	private GuardianClientAdapter adapter;
		
	public GuardianAbility(float damage, boolean mayMove, boolean mayCancel, boolean xray, int attackDuration, double maxDistance, Optional<SoundInstance> cancelAbilitySound, Optional<SoundInstance> successfullyAtackedSound)
	{
		this.damage = damage;
		this.mayMove = mayMove;
		this.mayCancel = mayCancel;
		this.xray = xray;
		this.attackDuration = attackDuration;
		this.cancelAbilitySound = cancelAbilitySound;
		this.successfullyAttackedSound = successfullyAtackedSound;
		this.maxDistance = maxDistance;
		
		// Holy. fck.
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GuardianClientAdapterInstance.createInstance(adapter -> this.adapter = adapter, entity -> isTracked(entity)));
	}
	
	public float getDamage()
	{
		return damage;
	}
	
	public double getMaxDistance()
	{
		return maxDistance;
	}

	public boolean mayMove()
	{
		return mayMove;
	}
	
	public boolean mayCancel()
	{
		return mayCancel;
	}
	
	public boolean mayXray()
	{
		return xray;
	}
	
	public int getAttackDuration()
	{
		return attackDuration;
	}
	
	public Optional<SoundInstance> getCancelAbilitySound()
	{
		return cancelAbilitySound;
	}
	
	public Optional<SoundInstance> getSuccessfullyAttackedSound()
	{
		return successfullyAttackedSound;
	}
	
	@Override
	public void onRegister()
	{
		super.onRegister();
		
		if(adapter != null)
			adapter.enableAdapter();
	}
	
	@Override
	public void onUnregister()
	{
		super.onUnregister();
		
		if(adapter != null)
			adapter.disableAdapter();
	}
	
	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!player.level.isClientSide)
		{
			// Cancel this shit
			if(isAbilityActive(player))
			{
				if(mayCancel)
				{
					cancelAbilitySound.ifPresent(sound -> sound.playSoundAt(player));
					GuardianBeamCapabilityHandler.INSTANCE.unattackServer(player);
					deapplyAbilityEffects(player);
				}
			}
			else
			{
				// Shoot ray to detect the entity that the player is currently looking at
				Vec3 from = player.position().add(Vec3.directionFromRotation(player.getRotationVector())).add(0, player.getEyeHeight(), 0);
				Vec3 to = Vec3.directionFromRotation(player.getRotationVector()).multiply(maxDistance, maxDistance, maxDistance).add(from);
				
				AABB aabb = new AABB(from, to);
				
				EntityHitResult result = ProjectileUtil.getEntityHitResult(player.level, player, from, to, aabb, entity -> entity instanceof LivingEntity);
				
				if(result != null)
				{
					BlockHitResult blockResult = player.level.clip(new ClipContext(from, result.getEntity().position().add(0, result.getEntity().getEyeHeight(), 0), Block.VISUAL, Fluid.NONE, null));
					
					if(blockResult == null || blockResult.getType() == Type.MISS && !isEntityOutOfRange(player, result.getEntity()))
					{
						GuardianBeamCapabilityHandler.INSTANCE.attackServer(player, result.getEntity(), getAttackDuration());
						applyAbilityEffects(player);
					}
				}
			}
		}
	}
	
	private boolean isEntityOutOfRange(Player ofPlayer, Entity entity)
	{
		return entity.position().distanceToSqr(ofPlayer.position()) > (maxDistance * maxDistance);
	}
	
	@SubscribeEvent
	public void onLivingJumpEvent(LivingJumpEvent event)
	{
		if(isTracked(event.getEntity()) && isAbilityActive((Player)event.getEntity()) && !mayMove)
		{
			event.getEntityLiving().setDeltaMovement(event.getEntityLiving().getDeltaMovement().x, 0, event.getEntityLiving().getDeltaMovement().z);
		}
	}
	
	@SubscribeEvent
	public void onGuardianStatusUpdate(GuardianAbilityStatusUpdateEvent event)
	{
		if(isTracked(event.getPlayer()))
		{				
			// Check if we are on the logical server
			if(!event.getPlayer().level.isClientSide)
			{
				if(event.isInvalidated())
				{
					deapplyAbilityEffects(event.getPlayer());
					return;
				}
				
				// Retrieve current entity
				Entity targettedEntity = ((ServerLevel)event.getPlayer().level).getEntity(event.getCapability().getAttackedEntityServer().get());
				
				// Check if entity is null or out of bounds
				if(targettedEntity == null || !targettedEntity.isAlive() || targettedEntity.isRemoved() || isEntityOutOfRange(event.getPlayer(), targettedEntity))
				{
					GuardianBeamCapabilityHandler.INSTANCE.unattackServer(event.getPlayer());
					return;
				}
				
				// Test if we may shoot through blocks
				if(!xray)
				{
					Vec3 from = event.getPlayer().position().add(Vec3.directionFromRotation(event.getPlayer().getRotationVector())).add(0, event.getPlayer().getEyeHeight(), 0);
					Vec3 to = targettedEntity.position().add(0, targettedEntity.getEyeHeight(), 0);
					
					// Re-check if entity is still not obstructed by block
					BlockHitResult blockResult = event.getPlayer().level.clip(new ClipContext(from, to, Block.VISUAL, Fluid.NONE, event.getPlayer()));
					
					// If a block was hit, cancel everything.
					if(blockResult != null && blockResult.getType() == Type.BLOCK)
					{
						GuardianBeamCapabilityHandler.INSTANCE.unattackServer(event.getPlayer());
						return;
					}
				}
				
				// Check if we are ready to attack
				if(event.getCapability().getAttackProgression() >= attackDuration)
				{
					// Attack entity
					Entity currentlyTargettedEntity = ((ServerLevel) event.getPlayer().level)
							.getEntity(event.getCapability().getAttackedEntityServer().get());
					
					currentlyTargettedEntity.hurt(DamageSource.indirectMagic(event.getPlayer(), event.getPlayer()), damage);
					
					// Set the capability accordingly and sync it to the client
					GuardianBeamCapabilityHandler.INSTANCE.unattackServer(event.getPlayer());
				}
			}			
		}		
	}
	
	@SubscribeEvent
	public void onMorphingServerEvent(PlayerMorphEvent.Server.Pre event)
	{
		// Cancel this event if the player may not 
		if(isTracked(event.getPlayer()) && isAbilityActive(event.getPlayer()) && !mayCancel)
			event.setCanceled(true);
	}
	
	// Apply slowness to the player performing the attack if necessary.
	private void applyAbilityEffects(Player player)
	{
		if(!mayMove && !player.level.isClientSide)
		{
			player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(am);
		}			
	}
	
	// Remove slowness again when we are done attacking
	private void deapplyAbilityEffects(Player player)
	{
		if(!mayMove && !player.level.isClientSide)
		{
			AttributeInstance inst = player.getAttribute(Attributes.MOVEMENT_SPEED);
			
			if(inst.hasModifier(am))
				inst.removeModifier(am);
		}
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		if(adapter != null && player.level.isClientSide)
			adapter.playGuardianSound(player);
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
		
		if(!player.level.isClientSide)
		{
			IGuardianBeamCapability cap = getNullableBeamCap(player);
			if (cap != null && cap.getAttackedEntity().isPresent())
			{
				GuardianBeamCapabilityHandler.INSTANCE.unattackServer(player);
			}
			
			deapplyAbilityEffects(player);
		}
	}
	
	private boolean isAbilityActive(Player player)
	{
		IGuardianBeamCapability cap = getNullableBeamCap(player);
		
		if(cap == null)
			return false;
		else
			return cap.getAttackedEntity().isPresent();
	}
	
	private IGuardianBeamCapability getNullableBeamCap(Player player)
	{
		return player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).resolve().orElse(null);
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
