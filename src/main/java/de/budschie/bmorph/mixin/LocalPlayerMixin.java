package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin
{
	@Inject(at = @At("TAIL"), method = "serverAiStep")
	private void serverAiStep(CallbackInfo callback)
	{
		LocalPlayer thisObj = (LocalPlayer) ((Object)this);
		
		if(isControlledCamera())
		{
			IMorphCapability cap = MorphUtil.getCapOrNull(thisObj);
			
			if(cap != null && cap.getCurrentMorph().isPresent() && cap.getCurrentMorph().get().getEntityType() != EntityType.PLAYER)
			{
				thisObj.xxa *= thisObj.getSpeed();
				thisObj.zza *= thisObj.getSpeed();
			}
		}
	}
	
	@Shadow
	public abstract boolean isControlledCamera();
}
