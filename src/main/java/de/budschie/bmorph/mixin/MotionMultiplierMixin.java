package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.budschie.bmorph.events.MotionMultiplierEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;

@Mixin(value = Entity.class)
public abstract class MotionMultiplierMixin
{	
	@Shadow
	private Vector3d motionMultiplier;
	
	@Shadow 
	
	@Inject(at = @At("HEAD"), method = "setMotionMultiplier(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/vector/Vector3d;)V", cancellable = true)
	private void onSetMotionMultiplier(BlockState blockState, Vector3d vector3d, CallbackInfo info)
	{
		Entity thisInstance = ((Entity)(Object)this);
		MotionMultiplierEvent event = new MotionMultiplierEvent(vector3d, thisInstance, blockState, thisInstance.getEntityWorld());
		
		boolean cancelled = MinecraftForge.EVENT_BUS.post(event);
		
		if(cancelled)
		{
			info.cancel();
		}
		else if(event.isDirty())
		{			
			vector3d = event.getNewMotionMultiplier();
		}
	}
}
