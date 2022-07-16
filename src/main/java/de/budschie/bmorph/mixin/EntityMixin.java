package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.budschie.bmorph.capabilities.proxy_entity_cap.ProxyEntityCapabilityInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class EntityMixin
{
	@Inject(at = @At("HEAD"), method = "playSound")
	public void playSound(SoundEvent pSound, float pVolume, float pPitch, CallbackInfo callbackInfo)
	{
		Entity thisInstance = (Entity) ((Object)this);
		
		if(!thisInstance.isSilent())
		{
			thisInstance.getCapability(ProxyEntityCapabilityInstance.PROXY_ENTITY_CAP).ifPresent(cap ->
			{
				if(cap.isProxyEntity())
				{
					thisInstance.level.playSound(Minecraft.getInstance().player, thisInstance.getX(), thisInstance.getY(), thisInstance.getZ(), pSound, thisInstance.getSoundSource(), pVolume, pPitch);
				}
			});
		}
	}
}
