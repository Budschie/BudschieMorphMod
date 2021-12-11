package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.phantom_glide.ChargeDirection;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityAttacher;
import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityHandler;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatus;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatusChangedEvent;
import de.budschie.bmorph.capabilities.phantom_glide.IGlideCapability;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.AbstractEventAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
public class PhantomAbility extends AbstractEventAbility
{
	public static final Codec<PhantomAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("charging_speed").forGetter(PhantomAbility::getChargingSpeed),
			Codec.FLOAT.fieldOf("max_flight_speed_x").forGetter(PhantomAbility::getMaxFlightSpeedX),
			Codec.FLOAT.fieldOf("min_flight_speed_y").forGetter(PhantomAbility::getMinFlightSpeedY),
			Codec.FLOAT.fieldOf("max_flight_speed_z").forGetter(PhantomAbility::getMaxFlightSpeedZ),
			Codec.INT.fieldOf("charging_ticks").forGetter(PhantomAbility::getMaxChargingTicks),
			Codec.INT.fieldOf("transition_ticks").forGetter(PhantomAbility::getTransitionTicks),
			ModCodecs.ABILITY.listOf().optionalFieldOf("gliding_abilities", Arrays.asList()).forGetter(PhantomAbility::getGlidingAbilities))
			.apply(instance, PhantomAbility::new));
	
	private float chargingSpeed;
	private float maxFlightSpeedX;
	private float minFlightSpeedY;
	private float maxFlightSpeedZ;
	private int maxChargingTicks;
	private int transitionTicks;
	
	private List<Ability> glidingAbilities;
	
	public PhantomAbility(float chargingSpeed, float maxFlightSpeedX, float minFlightSpeedY, float maxFlightSpeedZ, int maxChargingTicks, int transitionTicks, List<Ability> glidingAbilities)
	{
		this.chargingSpeed = chargingSpeed;
		this.maxFlightSpeedX = maxFlightSpeedX;
		this.minFlightSpeedY = minFlightSpeedY;
		this.maxFlightSpeedZ = maxFlightSpeedZ;
		this.maxChargingTicks = maxChargingTicks;
		this.transitionTicks = transitionTicks;
		
		this.glidingAbilities = glidingAbilities;
	}
	
	private void applyGlidingAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			for(Ability ability : glidingAbilities)
				cap.applyAbility(player, ability);
		});
	}
	
	private void deapplyGlidingAbilities(Player player)
	{
		MorphUtil.processCap(player, cap ->
		{
			for(Ability ability : glidingAbilities)
				cap.deapplyAbility(player, ability);
		});
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem)
	{
		super.enableAbility(player, enabledItem);
		
		player.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
		{
			if(cap.getGlideStatus() == GlideStatus.GLIDE)
				applyGlidingAbilities(player);
		});
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		player.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
		{
			// If we charge and then press the ability button, we should stop charging
			if(cap.getGlideStatus() == GlideStatus.CHARGE)
				GlideCapabilityHandler.stopChargingServer(player);
			// If we are gliding and press the ability button, we should start to charge
			else if (cap.getGlideStatus() == GlideStatus.GLIDE)
			{
				ChargeDirection dir;
				
				if(player.getRotationVector().x < 0)
					dir = ChargeDirection.UP;
				else
					dir = ChargeDirection.DOWN;
				
				GlideCapabilityHandler.startChargingServer(player, transitionTicks, maxChargingTicks, dir);
			}
			// If we are on the ground and press the ability button, we should start to glide
			else if(cap.getGlideStatus() == GlideStatus.STANDARD)
			{
				if(!cancelAbility(player))
					GlideCapabilityHandler.glideServer(player);
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
			event.player.getCapability(GlideCapabilityAttacher.GLIDE_CAP).ifPresent(cap ->
			{
				if(cap.getGlideStatus() == GlideStatus.CHARGE)
				{
					if(cap.getChargeDirection() != null)
						event.player.setDeltaMovement(getChargeMovement(cap));
					
					if(!event.player.level.isClientSide() && cancelAbility(event.player))
						GlideCapabilityHandler.standardServer(event.player);
				}
				else if(cap.getGlideStatus() == GlideStatus.GLIDE || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
				{
					if(cancelAbility(event.player))
					{
						event.player.stopFallFlying();
						
						if(!event.player.level.isClientSide())
							GlideCapabilityHandler.standardServer(event.player);
					}
					else
					{
						event.player.startFallFlying();
						
						Vec3 playerForward = event.player.getForward();
						Vec3 playerMotion = event.player.getDeltaMovement();
						
						Vec3 newMovement = new Vec3(Math.min(playerForward.x * maxFlightSpeedX, playerMotion.x), Math.max(playerForward.y * minFlightSpeedY, playerMotion.y), Math.min(playerForward.z * maxFlightSpeedZ, playerMotion.z));
						
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
	
	private boolean cancelAbility(Player player)
	{
		return player.isOnGround() || player.isInWaterOrBubble();
	}
	
	private Vec3 getChargeMovement(IGlideCapability cap)
	{
		return cap.getChargeDirection().getMovementDirection().multiply(1, chargingSpeed, 1);
	}
	
	public List<Ability> getGlidingAbilities()
	{
		return glidingAbilities;
	}

	public float getChargingSpeed()
	{
		return chargingSpeed;
	}

	public float getMaxFlightSpeedX()
	{
		return maxFlightSpeedX;
	}
	
	public float getMinFlightSpeedY()
	{
		return minFlightSpeedY;
	}
	
	public float getMaxFlightSpeedZ()
	{
		return maxFlightSpeedZ;
	}

	public int getMaxChargingTicks()
	{
		return maxChargingTicks;
	}

	public int getTransitionTicks()
	{
		return transitionTicks;
	}
}
