package de.budschie.bmorph.mixin;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.util.ProtectedMethodAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(Player.class)
public class PlayerSoundMixin
{
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_HURT_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_7975_", DamageSource.class);
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_DRINKING_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_7838_", ItemStack.class);
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_EATING_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_7866_", ItemStack.class);
	
	private static final ProtectedMethodAccess<Entity, SoundEvent> GET_SWIM_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_5501_");
	private static final ProtectedMethodAccess<Entity, SoundEvent> GET_SWIM_SPLASH_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_5509_");
	private static final ProtectedMethodAccess<Entity, SoundEvent> GET_SWIM_HIGH_SPEED_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_5508_");
	
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_DEATH_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_5592_");
	
	@Inject(at = @At("HEAD"), method = "getHurtSound", cancellable = true)
	private void getHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementLiving(hurtSound, living -> Optional.of(GET_HURT_SOUND.getValue(living, damageSource)));
	}
	
	@Inject(at = @At("HEAD"), method = "getEatingSound", cancellable = true)
	private void getEatingSound(DamageSource stack, CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementLiving(hurtSound, living -> Optional.of(GET_DRINKING_SOUND.getValue(living, stack)));
	}
	
	@Inject(at = @At("HEAD"), method = "getDrinkingSound", cancellable = true)
	private void getDrinkingSound(ItemStack stack, CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementLiving(hurtSound, living -> Optional.of(GET_EATING_SOUND.getValue(living, stack)));
	}
	
	@Inject(at = @At("HEAD"), method = "getSwimSound", cancellable = true)
	private void getSwimSound(CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementEntity(hurtSound, living -> Optional.of(GET_SWIM_SOUND.getValue(living)));
	}
	
	@Inject(at = @At("HEAD"), method = "getSwimSplashSound", cancellable = true)
	private void getSwimSplashSound(CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementEntity(hurtSound, living -> Optional.of(GET_SWIM_SPLASH_SOUND.getValue(living)));
	}
	
	@Inject(at = @At("HEAD"), method = "getSwimHighSpeedSplashSound", cancellable = true)
	private void getSwimHighSpeedSplashSound(CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementEntity(hurtSound, living -> Optional.of(GET_SWIM_HIGH_SPEED_SOUND.getValue(living)));
	}
	
	@Inject(at = @At("HEAD"), method = "getDeathSound", cancellable = true)
	private void getDeathSound(CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacementLiving(hurtSound, living -> Optional.of(GET_DEATH_SOUND.getValue(living)));
	}
	
	private void handleSoundReplacementLiving(CallbackInfoReturnable<SoundEvent> callback, Function<LivingEntity, Optional<SoundEvent>> soundSupplier)
	{
		handleSoundReplacementEntity(callback, entity ->
		{
			if(entity instanceof LivingEntity living)
			{
				return soundSupplier.apply(living);
			}
			else
			{
				return Optional.empty();
			}
		});
	}
	
	private void handleSoundReplacementEntity(CallbackInfoReturnable<SoundEvent> callback, Function<Entity, Optional<SoundEvent>> soundSupplier)
	{
		Player thisInstance = (Player) ((Object)this);
		
		LazyOptional<IMorphCapability> renderDataOpt = thisInstance.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
		if(renderDataOpt.isPresent())
		{
			IMorphCapability cap = renderDataOpt.resolve().get();
				
			Optional<Entity> entity = cap.getCurrentMorphEntity();
				
			if(entity.isPresent())
			{
				soundSupplier.apply(entity.get()).ifPresent(soundEvent -> callback.setReturnValue(soundEvent));
			}
		}
	}
}
