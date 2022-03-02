package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.phantom_glide.ChargeDirection;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityHandler;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityInstance;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatus;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatusChangedEvent;
import de.budschie.bmorph.capabilities.phantom_glide.IGlideCapability;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// idea: When having this ability, it is as if you were to have an elytra equipped.
// You may also give yourself a boost by pressing a key, and you can only fly for some time
// When pressing Y:
//  1. Glide in the direction that you are currently looking.
// 2. After gliding, stay in the air on the same y level
// 3. Glide
// 4. Repeat
public class PhantomAbility extends Ability
{
	public static final Codec<PhantomAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("charging_speed").forGetter(PhantomAbility::getChargingSpeed),
			ModCodecs.VECTOR_3D.fieldOf("min_flight_speed").forGetter(PhantomAbility::getMinFlightSpeed),
			ModCodecs.VECTOR_3D.fieldOf("max_flight_speed").forGetter(PhantomAbility::getMaxFlightSpeed),
			Codec.INT.fieldOf("charging_ticks").forGetter(PhantomAbility::getMaxChargingTicks),
			Codec.INT.fieldOf("transition_ticks").forGetter(PhantomAbility::getTransitionTicks),
			ModCodecs.ABILITY.listOf().optionalFieldOf("gliding_abilities", Arrays.asList()).forGetter(PhantomAbility::getGlidingAbilities))
			.apply(instance, PhantomAbility::new));
	
	private float chargingSpeed;
	private Vec3 minFlightSpeed;
	private Vec3 maxFlightSpeed;
	private int maxChargingTicks;
	private int transitionTicks;
	
	private List<LazyOptional<Ability>> glidingAbilities;
	
	public PhantomAbility(float chargingSpeed, Vec3 minFlightSpeed, Vec3 maxFlightSpeed, int maxChargingTicks, int transitionTicks, List<LazyOptional<Ability>> glidingAbilities)
	{
		this.chargingSpeed = chargingSpeed;
		this.minFlightSpeed = minFlightSpeed;
		this.maxFlightSpeed = maxFlightSpeed;
		this.maxChargingTicks = maxChargingTicks;
		this.transitionTicks = transitionTicks;
		
		this.glidingAbilities = glidingAbilities;
	}
	
	private void applyGlidingAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			for(LazyOptional<Ability> ability : glidingAbilities)
				ability.ifPresent(resolved -> cap.applyAbility(resolved));
		});
	}
	
	private void deapplyGlidingAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			for(LazyOptional<Ability> ability : glidingAbilities)
				ability.ifPresent(resolved -> cap.deapplyAbility(resolved));
		});
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
		
		player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
		{
			if(cap.getGlideStatus() == GlideStatus.GLIDE)
				applyGlidingAbilities(player);
		});
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
		{
			// If we charge and then press the ability button, we should stop charging
			if(cap.getGlideStatus() == GlideStatus.CHARGE)
				GlideCapabilityHandler.INSTANCE.stopChargingServer(player);
			// If we are gliding and press the ability button, we should start to charge
			else if (cap.getGlideStatus() == GlideStatus.GLIDE)
			{
				ChargeDirection dir;
				
				if(player.getRotationVector().x < 0)
					dir = ChargeDirection.UP;
				else
					dir = ChargeDirection.DOWN;
				
				GlideCapabilityHandler.INSTANCE.startChargingServer(player, transitionTicks, maxChargingTicks, dir);
			}
			// If we are on the ground and press the ability button, we should start to glide
			else if(cap.getGlideStatus() == GlideStatus.STANDARD)
			{
				if(!cancelAbility(player))
					GlideCapabilityHandler.INSTANCE.glideServer(player);
			}
		});
	}
	
	@SubscribeEvent
	public void onGlideStatusChanged(GlideStatusChangedEvent event)
	{
		if(isTracked(event.getPlayer()))
		{
			if(event.getNewGlideStatus() == GlideStatus.GLIDE)
			{
				// If we start gliding, we should apply gliding abilities
				applyGlidingAbilities(event.getPlayer());
			}
			else if(event.getOldGlideStatus() == GlideStatus.GLIDE)
			{
				// if we stop gliding, we should not apply gliding abilities
				deapplyGlidingAbilities(event.getPlayer());
			}
		}
	}
	
	@SubscribeEvent
	public void onUpdatePlayer(PlayerTickEvent event)
	{
		if(event.phase == Phase.END && isTracked(event.player))
		{
			event.player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
			{
				if(cap.getGlideStatus() == GlideStatus.CHARGE)
				{
					if(cap.getChargeDirection() != null)
						event.player.setDeltaMovement(getChargeMovement(cap));
					
					if(!event.player.level.isClientSide() && cancelAbility(event.player))
						GlideCapabilityHandler.INSTANCE.standardServer(event.player);
				}
				else if(cap.getGlideStatus() == GlideStatus.GLIDE || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
				{
					if(cancelAbility(event.player))
					{
						event.player.stopFallFlying();
						
						if(!event.player.level.isClientSide())
							GlideCapabilityHandler.INSTANCE.standardServer(event.player);
					}
					else
					{
						event.player.startFallFlying();
						
						Vec3 playerForward = event.player.getForward();
						Vec3 playerMotion = event.player.getDeltaMovement();
						
//						Vec3 newMovement = new Vec3(clamp(-maxFlightSpeedX, maxFlightSpeedX, playerMotion.x), Math.min(playerForward.y * minFlightSpeedY, playerMotion.y), clamp(-maxFlightSpeedZ, maxFlightSpeedZ, playerMotion.z));
						// We should probably implement a min and max value
						
						Vec3 newMovement = 
								new Vec3(clamp(-maxFlightSpeed.x, maxFlightSpeed.x, minormax(playerForward.x * minFlightSpeed.x, playerMotion.x)),
										clamp(-maxFlightSpeed.y, maxFlightSpeed.y, minormax(playerForward.y * minFlightSpeed.y, playerMotion.y)),
										clamp(-maxFlightSpeed.z, maxFlightSpeed.z, minormax(playerForward.z * minFlightSpeed.z, playerMotion.z)));
						
						event.player.setDeltaMovement(newMovement);
					}
				}
				
				if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
				{
					boolean invert = cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN;
					
					float progress = (float) Math.pow(((float)cap.getTransitionTime()) / ((float)cap.getMaxTransitionTime()), 0.1f);
					
					if(invert)
						progress = 1 - progress;
					
					if(!Float.isFinite(progress))
						progress = 0;
					
					event.player.setDeltaMovement(event.player.getDeltaMovement().lerp(getChargeMovement(cap), progress));
				}
			});
		}
	}
	
	// Does a min operation if the first number is negative, otherwise we'll do a max operation
	public double minormax(double a, double b)
	{
		if(Math.signum(a) < 0)
			return Math.min(a, b);
		else
			return Math.max(a, b);
	}
	
	private double clamp(double smallestValue, double largestValue, double number)
	{
		if(number < smallestValue)
			return smallestValue;
		
		if(number > largestValue)
			return largestValue;
		
		return number;
	}
	
	private boolean cancelAbility(Player player)
	{
		return player.isOnGround() || player.isInWaterOrBubble();
	}
	
	private Vec3 getChargeMovement(IGlideCapability cap)
	{
		return cap.getChargeDirection().getMovementDirection().multiply(1, chargingSpeed, 1);
	}
	
	public List<LazyOptional<Ability>> getGlidingAbilities()
	{
		return glidingAbilities;
	}

	public float getChargingSpeed()
	{
		return chargingSpeed;
	}

	public Vec3 getMinFlightSpeed()
	{
		return minFlightSpeed;
	}
	
	public Vec3 getMaxFlightSpeed()
	{
		return maxFlightSpeed;
	}

	public int getMaxChargingTicks()
	{
		return maxChargingTicks;
	}

	public int getTransitionTicks()
	{
		return transitionTicks;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
