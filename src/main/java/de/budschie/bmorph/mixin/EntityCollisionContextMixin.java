package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.stand_on_fluid.StandOnFluidInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;

// This better work with my other mod
@Mixin(value = EntityCollisionContext.class)
public class EntityCollisionContextMixin
{
	@Shadow
	private Entity entity;
	
	@Inject(at = @At("TAIL"), method = "canStandOnFluid(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/FlowingFluid;)Z", cancellable = true)
	private void canStandOnFluidInject(FluidState fluidState, FlowingFluid flowingFluid, CallbackInfoReturnable<Boolean> callback)
	{
		if(entity instanceof Player player)
		{
			player.getCapability(StandOnFluidInstance.STAND_ON_FLUID_CAP).ifPresent(cap ->
			{
				// I don't really know what the last part of this if statement is but vanilla does it so I will inherit it
				if(cap.getAllowedFluids().contains(flowingFluid) && !fluidState.getType().isSame(flowingFluid))
				{
					callback.setReturnValue(true);
				}
			});
		}
	}
}
