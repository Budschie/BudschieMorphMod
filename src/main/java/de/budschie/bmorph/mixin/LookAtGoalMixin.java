package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(value = LookAtGoal.class)
public abstract class LookAtGoalMixin extends Goal
{
	@Shadow
	private Entity closestEntity;
	
	@Inject(method = "shouldExecute()Z", at = @At("TAIL"), cancellable = true)
	private void shouldExecute(CallbackInfoReturnable<Boolean> callback)
	{
		if(this.closestEntity != null && ServerSetup.server.getGameRules().getBoolean(BMorphMod.PREVENT_LOOKAT) && this.closestEntity instanceof PlayerEntity)
		{
			// Get capability and check if entity is morphed.
			IMorphCapability cap = MorphUtil.getCapOrNull((PlayerEntity) this.closestEntity);
			
			if(cap != null)
			{
				callback.setReturnValue(!cap.getCurrentMorph().isPresent());
			}
		}
	}
}
