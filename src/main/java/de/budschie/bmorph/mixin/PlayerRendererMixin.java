package de.budschie.bmorph.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

@Mixin(value = PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
	public PlayerRendererMixin(EntityRendererProvider.Context rendererManager,
			PlayerModel<AbstractClientPlayer> entityModelIn, float shadowSizeIn)
	{
		super(rendererManager, entityModelIn, shadowSizeIn);
	}
	
	@Inject(at = @At("HEAD"), method = "renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V", cancellable = true)
	private void renderRightHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, CallbackInfo info)
	{
		if(checkMorphPresent(playerIn))
		{
//			RenderHandler.checkCache(playerIn);
			info.cancel();
			
			Entity cachedEntity = playerIn.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().get().getOrCreateCachedEntity(playerIn); 
			EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(cachedEntity);
			
			if(renderer instanceof LivingEntityRenderer<?, ?>)
			{
				ModelPart armRenderer = null;

				LivingEntityRenderer<? super LivingEntity, ?> living = (LivingEntityRenderer<? super LivingEntity, ?>) renderer;
				
				if(living.model instanceof HumanoidModel<?>)
					armRenderer = ((HumanoidModel<?>)living.model).rightArm;
				
				if(living.model instanceof QuadrupedModel<?>)
					armRenderer = ((QuadrupedModel<?>)living.model).leftHindLeg;
				
				if(armRenderer != null)
					renderArm(false, playerIn, armRenderer, matrixStackIn, combinedLightIn, bufferIn, living, (LivingEntity)cachedEntity, combinedLightIn);
			}
		}
	}
	
	@Inject(at = @At("HEAD"), method = "renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;)V", cancellable = true)
	private void renderLeftHand(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
			AbstractClientPlayer playerIn, CallbackInfo info)
	{		
		if(checkMorphPresent(playerIn))
		{
//			RenderHandler.checkCache(playerIn);
			info.cancel();
			
			Entity cachedEntity = playerIn.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().get().getOrCreateCachedEntity(playerIn); 
			EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(cachedEntity);
			
			if(renderer instanceof LivingEntityRenderer<?, ?>)
			{
				ModelPart armRenderer = null;

				LivingEntityRenderer<? super LivingEntity, ?> living = (LivingEntityRenderer<? super LivingEntity, ?>) renderer;
				
				if(living.model instanceof HumanoidModel<?>)
					armRenderer = ((HumanoidModel<?>)living.model).rightArm;
				
				if(living.model instanceof QuadrupedModel<?>)
					armRenderer = ((QuadrupedModel<?>)living.model).rightFrontLeg;
				
				if(armRenderer != null)
					renderArm(true, playerIn, armRenderer, matrixStackIn, combinedLightIn, bufferIn, living, (LivingEntity)cachedEntity, combinedLightIn);
			}
		}
	}
	
	@Shadow
	private void setModelProperties(AbstractClientPlayer clientPlayer)
	{
	}
	
	private void renderArm(boolean isLeft, AbstractClientPlayer player, ModelPart arm, PoseStack matrixStack, int combinedLightIn, MultiBufferSource buffer, LivingEntityRenderer<? super LivingEntity, ?> renderer, LivingEntity entity, int light)
	{
		matrixStack.pushPose();

		// Fix for sheep and stuff like that
		if(renderer.model instanceof QuadrupedModel<?>)
			matrixStack.translate(isLeft ? .1 : -.1, -.6, .5);

		setModelProperties(player);
		
		renderer.model.attackTime = 0.0f;
		
		if(renderer instanceof HumanoidMobRenderer<?, ?>)
		{
			HumanoidMobRenderer<? super Mob, ?> casted = (HumanoidMobRenderer<? super Mob, ?>) renderer;
			casted.model.swimAmount = 0.0f;
			casted.model.crouching = false;
						
			//Temp.translateShitAndStuff(isLeft ? , matrixStack);
		}
		
		// DAMN IT JAVA
		// This is utterly fucking retarded
		if((renderer instanceof HumanoidMobRenderer<?, ?> || renderer.getModel() instanceof PlayerModel<?>) && isLeft && player.getMainArm() == HumanoidArm.LEFT && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
			matrixStack.translate(.65, 0, 0);

		
		renderer.model.setupAnim(entity, 0, 0, 0, 0, 0);
		
		arm.xRot = 0;
		arm.render(matrixStack, buffer.getBuffer(RenderType.entityCutout(renderer.getTextureLocation(entity))), combinedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStack.popPose();
	}
	
	private boolean checkMorphPresent(Player player)
	{
		return player != null && player.getCapability(MorphCapabilityAttacher.MORPH_CAP).isPresent() && player.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get().getCurrentMorph().isPresent();
	}
}
