package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import de.budschie.bmorph.attributes.AttributeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult.Type;

@Mixin(value = GameRenderer.class)
public class PickMixin
{
	@Shadow
	private Minecraft minecraft;
	
	// Changes the pick constant for survival mode
	@ModifyConstant(method = "pick", constant = @Constant(doubleValue = 9.0D))
	private double changeMaxSurvivalModeRange(double old)
	{
		if(minecraft.cameraEntity instanceof Player)
		{
			double newValue = AttributeUtil.getAttackRange((Player) minecraft.cameraEntity);
			return newValue * newValue;
		}
		
		return old;
	}
	
	@ModifyVariable(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"), ordinal = 1)
	private double changeD1(double d1Old)
	{
		if (minecraft.cameraEntity instanceof Player && (minecraft.hitResult == null || (minecraft.hitResult != null && this.minecraft.hitResult.getType() == Type.MISS)))
		{
			double newValue = AttributeUtil.getAttackRange((Player) minecraft.cameraEntity);
			return newValue * newValue;
		}
		
		return d1Old;
	}
	
	// This is the mixin modifying the d0 double (the double that sets the pick range) We don't do this at the beginning because that would also extend the block range and not only the attack range
	@ModifyVariable(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"), ordinal = 0)
	private double changeD0(double d0Old)
	{
		if (minecraft.cameraEntity instanceof Player)
		{
			double newValue = AttributeUtil.getAttackRange((Player) minecraft.cameraEntity);
			return newValue;
		}
		
		return d0Old;
	}
}
