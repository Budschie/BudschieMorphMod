package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.speed_of_morph_cap.IPlayerUsingSpeedOfMorph;
import de.budschie.bmorph.capabilities.speed_of_morph_cap.PlayerUsingSpeedOfMorphInstance;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.util.LazyOptional;

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
			LazyOptional<IPlayerUsingSpeedOfMorph> applySpeedToAcc = thisObj.getCapability(PlayerUsingSpeedOfMorphInstance.SPEED_OF_MORPH_CAP);
			
			if(cap != null && cap.getCurrentMorph().isPresent() && applySpeedToAcc.isPresent() && applySpeedToAcc.resolve().get().isUsingSpeedOfMorph() && cap.getCurrentMorph().get().getEntityType() != EntityType.PLAYER)
			{
				thisObj.xxa *= thisObj.getSpeed();
				thisObj.zza *= thisObj.getSpeed();
			}
		}
	}
	
	@Shadow
	public abstract boolean isControlledCamera();
}
