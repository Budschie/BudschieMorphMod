package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.capabilities.proxy_entity_cap.ProxyEntityCapabilityInstance;
import de.budschie.bmorph.util.ProtectedMethodAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(Entity.class)
public class EntityMixin
{
	private static final ProtectedMethodAccess<Entity, SoundEvent> PLAY_STEP_SOUND = new ProtectedMethodAccess<>(Entity.class, "m_7355_", BlockPos.class, BlockState.class);

	@Inject(at = @At("HEAD"), method = "playSound")
	public void playSound(SoundEvent pSound, float pVolume, float pPitch, CallbackInfo callbackInfo)
	{
		Entity thisInstance = (Entity) ((Object)this);
		
		if(thisInstance.getLevel().isClientSide() && !thisInstance.isSilent())
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
	
	@Inject(at = @At("HEAD"), method = "playStepSound", cancellable = true)
	public void playStepSound(BlockPos blockPos, BlockState block, CallbackInfo callbackInfo)
	{
		Entity thisInstance = (Entity) ((Object)this);
		
		if(thisInstance.getLevel().isClientSide() && thisInstance instanceof Player player)
		{
			LazyOptional<IRenderDataCapability> renderDataOpt = player.getCapability(RenderDataCapabilityProvider.RENDER_CAP);
			
			if(renderDataOpt.isPresent())
			{
				IRenderDataCapability cap = renderDataOpt.resolve().get();
				
				Entity entity = cap.getOrCreateCachedEntity(player);
				
				if(entity != null)
				{
					PLAY_STEP_SOUND.getValue(entity, blockPos, block);
					callbackInfo.cancel();
				}
			}
		}
	}
}
