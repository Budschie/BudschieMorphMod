package de.budschie.bmorph.entity.rendering;

import java.util.UUID;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import de.budschie.bmorph.entity.MorphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MorphEntityRenderer extends EntityRenderer<MorphEntity>
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	WeakHashMap<UUID, Entity> entityCache = new WeakHashMap<>();

	public MorphEntityRenderer(EntityRendererProvider.Context renderManager)
	{
		super(renderManager);
	}

	@Override
	public ResourceLocation getTextureLocation(MorphEntity entity)
	{
		return null;
	}

	@Override
	public void render(MorphEntity entity, float something, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light)
	{
		// TODO: Clean this up
		Entity toRender = entityCache.get(entity.getUUID());

		if (toRender == null)
		{
			// Check if the entity was not loaded in successfull
			if (entityCache.containsKey(entity.getUUID()))
			{
				return;
			} else
			{
				toRender = entity.getMorphItem().createEntity(entity.level);

				// If we did not succeed, set entityCache value to null!
				if (toRender == null)
				{
					entityCache.put(entity.getUUID(), null);
					return;
				}

				if (toRender instanceof LivingEntity entityLiving)
				{
					entityLiving.deathTime = 0;
					entityLiving.hurtTime = 0;
				}

				entityCache.put(entity.getUUID(), toRender);
			}
		}

		toRender.tickCount = entity.tickCount;

		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(toRender);

		toRender.xo = entity.xo;
		toRender.yo = entity.yo;
		toRender.zo = entity.zo;

		toRender.setPos(entity.getX(), entity.getY(), entity.getZ());

		matrixStack.mulPose(Vector3f.YP.rotationDegrees(((toRender.tickCount + partialTicks) * 5f) % 360));
		matrixStack.translate(0, Math.sin((toRender.tickCount + partialTicks) * 0.25f) * 0.15f + 0.15f, 0);

		manager.render(toRender, 0, partialTicks, matrixStack, renderType ->
		{
			final VertexConsumer builder;

			if (renderType instanceof RenderType.CompositeRenderType)
			{
				if (((RenderType.CompositeRenderType) renderType).state.textureState.cutoutTexture().isEmpty())
				{
					LOGGER.error("If you see this, then there is an error with rendering that you should report as a bug.");
					return buffer.getBuffer(renderType);
				} else
				{
					RenderType newType = RenderType.entityTranslucent(((RenderType.CompositeRenderType) renderType).state.textureState.cutoutTexture().get());

					if (!newType.format().equals(renderType.format()))
						return buffer.getBuffer(renderType);

					builder = buffer.getBuffer(newType);
				}
//				builder = buffer.getBuffer(renderType);

//				if(rs.isPresent())
//				{
//					RenderType newType = RenderType.entityTranslucent(rs.get());
//					
//					if(!newType.format().equals(renderType.format()))
//						return buffer.getBuffer(renderType); 
//					
//					builder = buffer.getBuffer(newType);
//				}
//				else
//				{
//				}
			} else
			{
				LOGGER.error("If you see this, then there is an error with rendering that you should report as a bug.");
				return buffer.getBuffer(renderType);
			}

//				IVertexBuilder builder = (p_getBuffer_1_ instanceof RenderType.Type) ? buffer.getBuffer(RenderType.getEntityTranslucent(((RenderType.Type)p_getBuffer_1_).renderState.texture) : buffer.getBuffer(p_getBuffer_1_);
			return new VertexConsumer()
			{
				@Override
				public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV,
						float normalX, float normalY, float normalZ)
				{
					VertexConsumer.super.vertex(x, y, z, 255, 255, 255, 255, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
				}

				@Override
				public VertexConsumer uv(float u, float v)
				{
					return builder.uv(u, v);
				}

				@Override
				public VertexConsumer vertex(double x, double y, double z)
				{
					return builder.vertex(x, y, z);
				}

				@Override
				public VertexConsumer overlayCoords(int u, int v)
				{
					return builder.overlayCoords(u, v);
				}

				@Override
				public VertexConsumer normal(float x, float y, float z)
				{
					return builder.normal(x, y, z);
				}

				@Override
				public VertexConsumer uv2(int u, int v)
				{
					return builder.uv2(u, v);
				}

				@Override
				public void endVertex()
				{
					builder.endVertex();
				}

				@Override
				public VertexConsumer color(int red, int green, int blue, int alpha)
				{
					return builder.color(0, 150, 255, 135);
				}

				@Override
				public void defaultColor(int p_166901_, int p_166902_, int p_166903_, int p_166904_)
				{
					builder.defaultColor(p_166901_, p_166902_, p_166903_, p_166904_);
				}

				@Override
				public void unsetDefaultColor()
				{
					builder.unsetDefaultColor();
				}
			};
		}, light);
	}
}
