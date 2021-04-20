package de.budschie.bmorph.entity.rendering;

import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.budschie.bmorph.entity.MorphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class MorphEntityRenderer extends EntityRenderer<MorphEntity>
{
	WeakHashMap<UUID, Entity> entityCache = new WeakHashMap<>();
	
	public MorphEntityRenderer(EntityRendererManager renderManager)
	{
		super(renderManager);
	}

	@Override
	public ResourceLocation getEntityTexture(MorphEntity entity)
	{
		return null;
	}
	
	@Override
	public void render(MorphEntity entity, float something, float partialTicks, MatrixStack matrixStack,
			IRenderTypeBuffer buffer, int light)
	{
		Entity toRender = entityCache.get(entity.getUniqueID());
		
		if(toRender == null)
		{
			toRender = entity.getMorphItem().createEntity(entity.world);
			if(toRender instanceof LivingEntity)
			{
				LivingEntity entityLiving = (LivingEntity) toRender;
				entityLiving.deathTime = 0;
				entityLiving.hurtTime = 0;
			}
			entityCache.put(entity.getUniqueID(), toRender);
		}
		
		toRender.ticksExisted = entity.ticksExisted;
		
		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager().getRenderer(toRender);
//		
//		if(manager instanceof IEntityRenderer)
//		{
//			matrixStack.rotate(Vector3f.YP.rotationDegrees(((toRender.ticksExisted + partialTicks) * 5f) % 360));
//			matrixStack.translate(0, Math.sin((toRender.ticksExisted + partialTicks) * 0.25f) * 0.15f + 0.15f, 0);
//			matrixStack.rotate(Vector3f.XP.rotationDegrees(180));
//			matrixStack.translate(0.0D, (double)-1.501F, 0.0D);
//
//			@SuppressWarnings("unchecked")
//			IEntityRenderer<? extends Entity, ?> renderer = (IEntityRenderer<? extends Entity, ?>) manager;
//			renderer.getEntityModel().render(matrixStack, buffer.getBuffer(RenderType.getEntityTranslucentCull(manager.getEntityTexture(toRender))), light, LivingRenderer.getPackedOverlay((LivingEntity) toRender, 0), 0, .35f, 1f, .7f);
//		}
		
		manager.render(toRender, 0, partialTicks, matrixStack, buffer, light);
	}
}
