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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(LivingEntity.class)
public class LivingEntitySoundMixin
{
	private static final ProtectedMethodAccess<LivingEntity, Float> GET_SOUND_VOLUME = new ProtectedMethodAccess<>(LivingEntity.class, "m_6121_");
	private static final ProtectedMethodAccess<LivingEntity, Float> GET_VOICE_PITCH = new ProtectedMethodAccess<>(LivingEntity.class, "m_6100_");

	@Inject(at = @At("HEAD"), method = "getSoundVolume", cancellable = true)
	private void getSoundVolume(CallbackInfoReturnable<Float> callback)
	{
		if((Object)(this) instanceof Player player)
		{
			handlePitchVolumeReplacement(callback, livingEntity -> Optional.of(GET_SOUND_VOLUME.getValue(livingEntity)));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getVoicePitch", cancellable = true)
	private void getVoicePitch(CallbackInfoReturnable<Float> callback)
	{
		if((Object)(this) instanceof Player player)
		{
			handlePitchVolumeReplacement(callback, livingEntity -> Optional.of(GET_VOICE_PITCH.getValue(livingEntity)));
		}
	}
	
	private void handlePitchVolumeReplacement(CallbackInfoReturnable<Float> callback, Function<LivingEntity, Optional<Float>> floatSupplier)
	{
		Player thisInstance = (Player) ((Object)this);
		
		LazyOptional<IMorphCapability> renderDataOpt = thisInstance.getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
		if(renderDataOpt.isPresent())
		{
			IMorphCapability cap = renderDataOpt.resolve().get();
				
			Optional<Entity> entity = cap.getCurrentMorphEntity();
				
			if(entity.isPresent() && entity.get() instanceof LivingEntity living)
			{
				floatSupplier.apply(living).ifPresent(soundEvent -> callback.setReturnValue(soundEvent));
			}
		}
	}

}
