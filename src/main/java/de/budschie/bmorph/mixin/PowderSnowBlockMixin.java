package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.events.CanWalkOnPowderSnowEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;

@Mixin(value = PowderSnowBlock.class)
public class PowderSnowBlockMixin
{
	@Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
	private static void canEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> callback)
	{
		CanWalkOnPowderSnowEvent event = new CanWalkOnPowderSnowEvent(entity);
		
		MinecraftForge.EVENT_BUS.post(event);
		
		if(event.getResult() == Result.ALLOW)
			callback.setReturnValue(true);
		else if(event.getResult() == Result.DENY)
			callback.setReturnValue(false);
	}
}
