package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.custom_riding_data.CustomRidingDataInstance;
import de.budschie.bmorph.capabilities.custom_riding_data.ICustomRidingData;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(Player.class)
public class PlayerMixin
{
	// TODO: Make forge PR for this.
	@Inject(at = @At("HEAD"), method = "getMyRidingOffset", cancellable = true)
	private void getMyRidingOffset(CallbackInfoReturnable<Double> callback)
	{
		LazyOptional<ICustomRidingData> customRidingOffsetCap = ((Player)((Object)this)).getCapability(CustomRidingDataInstance.CUSTOM_RIDING_DATA_CAP);
		
		if(customRidingOffsetCap.isPresent() && customRidingOffsetCap.resolve().get().getCustomRidingOffset().isPresent())
		{
			callback.setReturnValue(customRidingOffsetCap.resolve().get().getCustomRidingOffset().get());
		}
	}	
}
