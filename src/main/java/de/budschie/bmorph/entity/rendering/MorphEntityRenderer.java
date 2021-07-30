package de.budschie.bmorph.entity.rendering;

import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import de.budschie.bmorph.entity.MorphEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

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
			
			if(toRender == null)
				throw new NullPointerException("The morph data could not be translated to an entity.");
			
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
		
		toRender.prevPosX = entity.prevPosX;
		toRender.prevPosY = entity.prevPosY;
		toRender.prevPosZ = entity.prevPosZ;
		
		toRender.setPosition(entity.getPosX(), entity.getPosY(), entity.getPosZ());
		
		matrixStack.rotate(Vector3f.YP.rotationDegrees(((toRender.ticksExisted + partialTicks) * 5f) % 360));
		matrixStack.translate(0, Math.sin((toRender.ticksExisted + partialTicks) * 0.25f) * 0.15f + 0.15f, 0);
		
		manager.render(toRender, 0, partialTicks, matrixStack, new IRenderTypeBuffer()
		{
			@Override
			public IVertexBuilder getBuffer(RenderType renderType)
			{								
				final IVertexBuilder builder;
				
				if(renderType instanceof RenderType.Type)
				{
					Optional<ResourceLocation> rs = ((RenderType.Type)renderType).renderState.texture.texture;
					
					if(rs.isPresent())
					{
						RenderType newType = RenderType.getEntityTranslucent(rs.get());
						
						if(!newType.getVertexFormat().equals(renderType.getVertexFormat()))
							return buffer.getBuffer(renderType); 
						
						builder = buffer.getBuffer(newType);
					}
					else
					{
						System.out.println("If you see this, then there is an error with rendering that you should report as a bug.");
						return buffer.getBuffer(renderType);
					}
				}
				else
				{
					System.out.println("If you see this, then there is an error with rendering that you should report as a bug.");
					return buffer.getBuffer(renderType);
				}
				
//				IVertexBuilder builder = (p_getBuffer_1_ instanceof RenderType.Type) ? buffer.getBuffer(RenderType.getEntityTranslucent(((RenderType.Type)p_getBuffer_1_).renderState.texture) : buffer.getBuffer(p_getBuffer_1_);
				return new IVertexBuilder()
				{
					@Override
					public void addVertex(float x, float y, float z, float red, float green, float blue, float alpha,
							float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY,
							float normalZ)
					{
						IVertexBuilder.super.addVertex(x, y, z, 255, 255, 255, 255, texU, texV, overlayUV, lightmapUV, normalX, normalY,
								normalZ);
					}
					
					@Override
					public IVertexBuilder tex(float u, float v)
					{
						return builder.tex(u, v);
					}
					
					@Override
					public IVertexBuilder pos(double x, double y, double z)
					{
						return builder.pos(x, y, z);
					}
					
					@Override
					public IVertexBuilder overlay(int u, int v)
					{
						return builder.overlay(u, v);
					}
					
					@Override
					public IVertexBuilder normal(float x, float y, float z)
					{
						return builder.normal(x, y, z);
					}
					
					@Override
					public IVertexBuilder lightmap(int u, int v)
					{
						return builder.lightmap(u, v);
					}
					
					@Override
					public void endVertex()
					{
						builder.endVertex();
					}
					
					@Override
					public IVertexBuilder color(int red, int green, int blue, int alpha)
					{
						return builder.color(0, 150, 255, 135);
					}
				};
			}
		}, light);	}
}
