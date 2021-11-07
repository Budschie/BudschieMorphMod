package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityHandler;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import de.budschie.bmorph.events.GuardianAbilityStatusUpdateEvent;
import de.budschie.bmorph.events.PlayerMorphEvent;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.configurable.client.GuardianClientAdapter;
import de.budschie.bmorph.morph.functionality.configurable.client.GuardianClientAdapterInstance;
import de.budschie.bmorph.util.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;

public class GuardianAbility extends AbstractEventAbility
{
	public static Codec<GuardianAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
	
	private AttributeModifier am = new AttributeModifier(UUID.randomUUID(), "no movement guardian", -1, Operation.MULTIPLY_TOTAL);
	
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
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!player.getEntityWorld().isRemote)
		{
			// Cancel this shit
			if(isAbilityActive(player))
			{
				if(mayCancel)
				{
					cancelAbilitySound.ifPresent(sound -> sound.playSoundAt(player));
					GuardianBeamCapabilityHandler.unattackServer(player);
					deapplyAbilityEffects(player);
				}
			}
			else
			{
				// Shoot ray to detect the entity that the player is currently looking at
				Vector3d from = player.getPositionVec().add(Vector3d.fromPitchYaw(player.getPitchYaw())).add(0, player.getEyeHeight(), 0);
				Vector3d to = Vector3d.fromPitchYaw(player.getPitchYaw()).mul(50, 50, 50).add(from);
				
				AxisAlignedBB aabb = new AxisAlignedBB(from, to);
				
				EntityRayTraceResult result = ProjectileHelper.rayTraceEntities(player.getEntityWorld(), player, from, to, aabb, entity -> entity instanceof LivingEntity);
				
				if(result != null)
				{
					BlockRayTraceResult blockResult = player.getEntityWorld().rayTraceBlocks(new RayTraceContext(from, result.getEntity().getPositionVec().add(0, result.getEntity().getEyeHeight(), 0), BlockMode.VISUAL, FluidMode.NONE, null));
					
					if(blockResult == null || blockResult.getType() == Type.MISS)
					{
						GuardianBeamCapabilityHandler.attackServer(player, result.getEntity(), getAttackDuration());
						applyAbilityEffects(player);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingJumpEvent(LivingJumpEvent event)
	{
		if(isTracked(event.getEntity()) && isAbilityActive((PlayerEntity)event.getEntity()) && !mayMove)
		{
			event.getEntityLiving().setMotion(event.getEntityLiving().getMotion().x, 0, event.getEntityLiving().getMotion().z);
		}
	}
	
	@SubscribeEvent
	public void onGuardianStatusUpdate(GuardianAbilityStatusUpdateEvent event)
	{
		if(isTracked(event.getPlayer()))
		{				
			// Check if we are on the logical server
			if(!event.getPlayer().getEntityWorld().isRemote)
			{
				// Retrieve current entity
				Entity targettedEntity = ((ServerWorld)event.getPlayer().getEntityWorld()).getEntityByUuid(event.getCapability().getAttackedEntityServer().get());
				
				// Check if entity is null or out of bounds
				if(targettedEntity == null || !targettedEntity.isAlive() || targettedEntity.getPositionVec().squareDistanceTo(event.getPlayer().getPositionVec()) > (maxDistance * maxDistance))
				{
					GuardianBeamCapabilityHandler.unattackServer(event.getPlayer());
					deapplyAbilityEffects(event.getPlayer());
					return;
				}
				
				// Test if we may shoot through blocks
				if(!xray)
				{
					Vector3d from = event.getPlayer().getPositionVec().add(Vector3d.fromPitchYaw(event.getPlayer().getPitchYaw())).add(0, event.getPlayer().getEyeHeight(), 0);
					Vector3d to = targettedEntity.getPositionVec().add(0, targettedEntity.getEyeHeight(), 0);
					
					// Re-check if entity is still not obstructed by block
					BlockRayTraceResult blockResult = event.getPlayer().getEntityWorld().rayTraceBlocks(new RayTraceContext(from, to, BlockMode.VISUAL, FluidMode.NONE, event.getPlayer()));
					
					// If a block was hit, cancel everything.
					if(blockResult != null && blockResult.getType() == Type.BLOCK)
					{
						GuardianBeamCapabilityHandler.unattackServer(event.getPlayer());
						deapplyAbilityEffects(event.getPlayer());
						return;
					}
				}
				
				// Check if we are ready to attack
				if(event.getCapability().getAttackProgression() >= attackDuration)
				{
					// Attack entity
					Entity currentlyTargettedEntity = ((ServerWorld) event.getPlayer().getEntityWorld())
							.getEntityByUuid(event.getCapability().getAttackedEntityServer().get());
					
					currentlyTargettedEntity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(event.getPlayer(), event.getPlayer()), damage);
					
					// Set the capability accordingly and sync it to the client
					GuardianBeamCapabilityHandler.unattackServer(event.getPlayer());
					
					// Remove the slowness effect if possible
					deapplyAbilityEffects(event.getPlayer());
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
	private void applyAbilityEffects(PlayerEntity player)
	{
		if(!mayMove && !player.getEntityWorld().isRemote)
		{
			player.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(am);
		}			
	}
	
	// Remove slowness again when we are done attacking
	private void deapplyAbilityEffects(PlayerEntity player)
	{
		if(!mayMove && !player.getEntityWorld().isRemote)
		{
			ModifiableAttributeInstance inst = player.getAttribute(Attributes.MOVEMENT_SPEED);
			
			if(inst.hasModifier(am))
				inst.removeModifier(am);
		}
	}
	
	@Override
	public void enableAbility(PlayerEntity player, MorphItem enabledItem)
	{
		super.enableAbility(player, enabledItem);
		
		if(adapter != null && player.getEntityWorld().isRemote)
			adapter.playGuardianSound(player);
	}
	
	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		if(!player.getEntityWorld().isRemote)
		{
			IGuardianBeamCapability cap = getNullableBeamCap(player);
			if (cap != null && cap.getAttackedEntity().isPresent())
			{
				GuardianBeamCapabilityHandler.unattackServer(player);
			}
			
			deapplyAbilityEffects(player);
		}
		
		super.disableAbility(player, disabledItem);
	}
	
	private boolean isAbilityActive(PlayerEntity player)
	{
		IGuardianBeamCapability cap = getNullableBeamCap(player);
		
		if(cap == null)
			return false;
		else
			return cap.getAttackedEntity().isPresent();
	}
	
	private IGuardianBeamCapability getNullableBeamCap(PlayerEntity player)
	{
		return player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).resolve().orElse(null);
	}
}
