package de.budschie.bmorph.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.render_handler.RenderHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(value = PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{
	public PlayerRendererMixin(EntityRendererManager rendererManager,
			PlayerModel<AbstractClientPlayerEntity> entityModelIn, float shadowSizeIn)
	{
		super(rendererManager, entityModelIn, shadowSizeIn);
	}
	
	@Inject(at = @At("HEAD"), method = "renderRightArm(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V", cancellable = true)
	private void renderRightArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			AbstractClientPlayerEntity playerIn, CallbackInfo info)
	{
		if(checkMorphPresent(playerIn))
		{
			RenderHandler.checkCache(playerIn);
			info.cancel();
			
			Entity cachedEntity = RenderHandler.cachedEntities.get(playerIn.getUniqueID()); 
			EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(cachedEntity);
			
			checkIfBiped(renderer, (casted, model) ->
			{
				renderArm(playerIn, casted.entityModel.bipedRightArm, matrixStackIn, combinedLightIn, bufferIn, casted, (MobEntity) cachedEntity, combinedLightIn);
			});
		}
	}
	
	@Inject(at = @At("HEAD"), method = "renderLeftArm(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;)V", cancellable = true)
	private void renderLeftArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
			AbstractClientPlayerEntity playerIn, CallbackInfo info)
	{
		if(checkMorphPresent(playerIn))
		{
			RenderHandler.checkCache(playerIn);
			info.cancel();
			
			Entity cachedEntity = RenderHandler.cachedEntities.get(playerIn.getUniqueID()); 
			EntityRenderer<?> renderer = Minecraft.getInstance().getRenderManager().getRenderer(cachedEntity);
			
			checkIfBiped(renderer, (casted, model) ->
			{
				renderArm(playerIn, casted.entityModel.bipedLeftArm, matrixStackIn, combinedLightIn, bufferIn, casted, (MobEntity) cachedEntity, combinedLightIn);
			});
		}
	}
	
	@Shadow
	private void setModelVisibilities(AbstractClientPlayerEntity clientPlayer)
	{
	}
	
	private void renderArm(AbstractClientPlayerEntity player, ModelRenderer arm, MatrixStack matrixStack, int combinedLightIn, IRenderTypeBuffer buffer, BipedRenderer<? super MobEntity, ?> renderer, MobEntity entity, int light)
	{
		setModelVisibilities(player);
		renderer.entityModel.swimAnimation = 0.0f;
		renderer.entityModel.swingProgress = 0.0f;
		renderer.entityModel.isSneak = false;
		renderer.entityModel.setRotationAngles(entity, 0, 0, 0, 0, 0);
		arm.rotateAngleX = 0;
		arm.render(matrixStack, buffer.getBuffer(RenderType.getEntityCutout(renderer.getEntityTexture(entity))), combinedLightIn, OverlayTexture.NO_OVERLAY);
	}
	
	private boolean checkMorphPresent(PlayerEntity player)
	{
		return player != null && player.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get().getCurrentMorph().isPresent();
	}
	
	private void checkIfBiped(EntityRenderer<?> renderer, BiConsumer<BipedRenderer<? super MobEntity, ?>, BipedModel<?>> bipedModel)
	{
		if(renderer instanceof BipedRenderer<?, ?>)
		{
			bipedModel.accept(((BipedRenderer<? super MobEntity, ?>)renderer), ((BipedRenderer<?, ?>)renderer).entityModel);
		}
	}
}
