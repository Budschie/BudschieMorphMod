package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.stand_on_fluid.StandOnFluidInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;

@Mixin(LivingEntity.class)
public class LivingEntityStandOnFluidMixin
{
	@Inject(method = "canStandOnFluid(Lnet/minecraft/world/level/material/FluidState;)Z", at = @At("HEAD"), cancellable = true)
	private void canStandOnFluidMixin(FluidState fluid, CallbackInfoReturnable<Boolean> callback)
	{
		Entity entity = (Entity) ((Object)this);
		
		if(entity instanceof Player player)
		{
			player.getCapability(StandOnFluidInstance.STAND_ON_FLUID_CAP).ifPresent(cap ->
			{
				if(cap.containsFluid(fluid.getType()))
					callback.setReturnValue(true);
			});
		}
	}
}
