package de.budschie.bmorph.mixin;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.capabilities.custom_riding_offset.CustomRidingOffsetInstance;
import de.budschie.bmorph.capabilities.custom_riding_offset.ICustomRidingOffset;
import de.budschie.bmorph.util.ProtectedMethodAccess;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(Player.class)
public class PlayerMixin
{
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_HURT_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_7975_", DamageSource.class);
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_DRINKING_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_7838_", ItemStack.class);
	private static final ProtectedMethodAccess<LivingEntity, SoundEvent> GET_EATING_SOUND = new ProtectedMethodAccess<>(LivingEntity.class, "m_7866_", ItemStack.class);
	
	@Inject(at = @At("HEAD"), method = "getHurtSound", cancellable = true)
	private void getHurtSound(DamageSource damageSource, CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacement(hurtSound, living -> GET_HURT_SOUND.getValue(living, damageSource));
	}
	
	@Inject(at = @At("HEAD"), method = "getEatingSound", cancellable = true)
	private void getEatingSound(DamageSource stack, CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacement(hurtSound, living -> GET_DRINKING_SOUND.getValue(living, stack));
	}
	
	@Inject(at = @At("HEAD"), method = "getDrinkingSound", cancellable = true)
	private void getDrinkingSound(ItemStack stack, CallbackInfoReturnable<SoundEvent> hurtSound)
	{
		handleSoundReplacement(hurtSound, living -> GET_EATING_SOUND.getValue(living, stack));
	}
	
	// TODO: Make forge PR for this.
	@Inject(at = @At("HEAD"), method = "getMyRidingOffset", cancellable = true)
	private void getMyRidingOffset(CallbackInfoReturnable<Double> callback)
	{
		LazyOptional<ICustomRidingOffset> customRidingOffsetCap = ((Player)((Object)this)).getCapability(CustomRidingOffsetInstance.CUSTOM_RIDING_OFFSET_CAP);
		
		if(customRidingOffsetCap.isPresent() && customRidingOffsetCap.resolve().get().getCustomRidingOffset().isPresent())
		{
			System.out.println("Returning custom r offset" + customRidingOffsetCap.resolve().get().getCustomRidingOffset().get());
			callback.setReturnValue(customRidingOffsetCap.resolve().get().getCustomRidingOffset().get());
		}
		
		System.out.println("C");
	}
	
	private void handleSoundReplacement(CallbackInfoReturnable<SoundEvent> hurtSound, Function<LivingEntity, SoundEvent> soundSupplier)
	{
		Player thisInstance = (Player) ((Object)this);
		
		if(thisInstance.getLevel().isClientSide())
		{
			LazyOptional<IRenderDataCapability> renderDataOpt = thisInstance.getCapability(RenderDataCapabilityProvider.RENDER_CAP);
			
			if(renderDataOpt.isPresent())
			{
				IRenderDataCapability cap = renderDataOpt.resolve().get();
				
				Entity entity = cap.getOrCreateCachedEntity(thisInstance);
				
				if(entity != null && entity instanceof LivingEntity living)
				{
					hurtSound.setReturnValue(soundSupplier.apply(living));
				}
			}
		}
	}
}
