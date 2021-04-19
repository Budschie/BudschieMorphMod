package de.budschie.bmorph.entity.rendering;

import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.budschie.bmorph.entity.MorphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
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
			entityCache.put(entity.getUniqueID(), toRender);
		}
		
		toRender.ticksExisted = entity.ticksExisted;
		
		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager().getRenderer(toRender);
		//matrixStack.mulPose(Vector3f.YP.rotationDegrees((entity.tickCount * partialTicks) % 360));
		
		// This may be problematic, but I don't care as I (think I) don't know the render type that is being used!
		//RenderSystem.color4f(0, .25f, 1f, .5f);

		manager.render(toRender, something, partialTicks, matrixStack, buffer, light);
	}
}
