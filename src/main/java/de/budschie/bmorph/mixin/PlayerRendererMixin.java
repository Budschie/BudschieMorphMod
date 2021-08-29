package de.budschie.bmorph.mixin;

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
import net.minecraft.client.renderer.entity.model.QuadrupedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

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
			
			if(renderer instanceof LivingRenderer<?, ?>)
			{
				ModelRenderer armRenderer = null;

				LivingRenderer<? super LivingEntity, ?> living = (LivingRenderer<? super LivingEntity, ?>) renderer;
				
				if(living.entityModel instanceof BipedModel<?>)
					armRenderer = ((BipedModel<?>)living.entityModel).bipedRightArm;
				
				if(living.entityModel instanceof QuadrupedModel<?>)
					armRenderer = ((QuadrupedModel<?>)living.entityModel).legFrontRight;
				
				if(armRenderer != null)
					renderArm(false, playerIn, armRenderer, matrixStackIn, combinedLightIn, bufferIn, living, (LivingEntity)cachedEntity, combinedLightIn);
			}
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
			
			if(renderer instanceof LivingRenderer<?, ?>)
			{
				ModelRenderer armRenderer = null;

				LivingRenderer<? super LivingEntity, ?> living = (LivingRenderer<? super LivingEntity, ?>) renderer;
				
				if(living.entityModel instanceof BipedModel<?>)
					armRenderer = ((BipedModel<?>)living.entityModel).bipedRightArm;
				
				if(living.entityModel instanceof QuadrupedModel<?>)
					armRenderer = ((QuadrupedModel<?>)living.entityModel).legFrontLeft;
				
				if(armRenderer != null)
					renderArm(true, playerIn, armRenderer, matrixStackIn, combinedLightIn, bufferIn, living, (LivingEntity)cachedEntity, combinedLightIn);
			}
		}
	}
	
	@Shadow
	private void setModelVisibilities(AbstractClientPlayerEntity clientPlayer)
	{
	}
	
	private void renderArm(boolean isLeft, AbstractClientPlayerEntity player, ModelRenderer arm, MatrixStack matrixStack, int combinedLightIn, IRenderTypeBuffer buffer, LivingRenderer<? super LivingEntity, ?> renderer, LivingEntity entity, int light)
	{
		matrixStack.push();

		// Fix for sheep and stuff like that
		if(renderer.entityModel instanceof QuadrupedModel<?>)
			matrixStack.translate(isLeft ? .1 : -.1, -.6, .5);

		setModelVisibilities(player);
		
		renderer.entityModel.swingProgress = 0.0f;
		
		if(renderer instanceof BipedRenderer<?, ?>)
		{
			BipedRenderer<? super MobEntity, ?> casted = (BipedRenderer<? super MobEntity, ?>) renderer;
			casted.entityModel.swimAnimation = 0.0f;
			casted.entityModel.isSneak = false;
						
			//Temp.translateShitAndStuff(isLeft ? , matrixStack);
		}
		
		// DAMN IT JAVA
		// This is utterly fucking retarded
		if((renderer instanceof BipedRenderer<?, ?> || renderer.getEntityModel() instanceof PlayerModel<?>) && isLeft && player.getPrimaryHand() == HandSide.LEFT && player.getHeldItem(Hand.MAIN_HAND).isEmpty())
			matrixStack.translate(.65, 0, 0);

		
		renderer.entityModel.setRotationAngles((LivingEntity) entity, 0, 0, 0, 0, 0);
		
		arm.rotateAngleX = 0;
		arm.render(matrixStack, buffer.getBuffer(RenderType.getEntityCutout(renderer.getEntityTexture(entity))), combinedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStack.pop();
	}
	
	private boolean checkMorphPresent(PlayerEntity player)
	{
		return player != null && player.getCapability(MorphCapabilityAttacher.MORPH_CAP).isPresent() && player.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get().getCurrentMorph().isPresent();
	}
}
