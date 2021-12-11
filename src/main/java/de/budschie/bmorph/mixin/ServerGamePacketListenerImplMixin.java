package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import de.budschie.bmorph.attributes.AttributeUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(value = ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin
{
	@Shadow
	ServerPlayer player;
	
	@ModifyConstant(method = "handleInteract", require = 2, constant = @Constant(doubleValue = 36.0D))
	private double changeRadius(double oldIn)
	{
		double allowedRange = AttributeUtil.getAttackRange(player);
		return allowedRange * allowedRange;
	}
}