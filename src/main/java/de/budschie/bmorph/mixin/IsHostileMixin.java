package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;

@Mixin(value = VillagerHostilesSensor.class)
public class IsHostileMixin
{
	@Inject(method = "isHostile(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void isHostileInjection(LivingEntity toCheck, CallbackInfoReturnable<Boolean> callback)
	{
		if(toCheck instanceof Player player)
		{
			IMorphCapability morphCap = MorphUtil.getCapOrNull(player);
			
			if(morphCap != null)
			{
				morphCap.getCurrentMorph().ifPresent(item ->
				{
					callback.setReturnValue(VillagerHostilesSensor.ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(item.getEntityType()));
				});
			}
		}
	}
	
	@Inject(method = "isClose(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void isCloseInjection(LivingEntity villager, LivingEntity hostileEntity, CallbackInfoReturnable<Boolean> callback)
	{
		if(hostileEntity instanceof Player player)
		{
			IMorphCapability morphCap = MorphUtil.getCapOrNull(player);
			
			if(morphCap != null)
			{
				morphCap.getCurrentMorph().ifPresent(item ->
				{
					float distance = VillagerHostilesSensor.ACCEPTABLE_DISTANCE_FROM_HOSTILES.getOrDefault(item.getEntityType(), 0.0f);
					
					callback.setReturnValue(villager.distanceToSqr(hostileEntity) <= (distance * distance));
				});
			}
		}
	}
}
