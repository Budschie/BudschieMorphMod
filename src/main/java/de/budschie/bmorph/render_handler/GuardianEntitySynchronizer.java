package de.budschie.bmorph.render_handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.GuardianEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// We do this shit instead of using an IEntitySynchronizer because we have to sync data with the cached entity, even if it is not rendered, as the synced data is used for the GuardianSound instance.
@EventBusSubscriber(value = Dist.CLIENT)
public class GuardianEntitySynchronizer
{	
	@SubscribeEvent
	public static void onPlayerTickEvent(PlayerTickEvent event)
	{
		if(event.side == LogicalSide.CLIENT && event.phase == Phase.START)
		{
			IGuardianBeamCapability beamCap = event.player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).resolve().orElse(null);

			if (beamCap != null)
			{
				Entity currentCachedEntity = RenderHandler.getCachedEntity(event.player);
				
				if(currentCachedEntity instanceof GuardianEntity)
				{
					GuardianEntity casted = (GuardianEntity) currentCachedEntity;
					
					casted.clientSideAttackTime = beamCap.getAttackProgression();
					
					if(beamCap.getAttackedEntity().isPresent())
					{
						casted.setTargetedEntity(beamCap.getAttackedEntity().get());
					}
					else
					{
						casted.setTargetedEntity(0);
					}
					
					casted.clientSideTouchedGround = casted.getMotion().y < 0.0D && casted.world.isTopSolid(casted.getPosition().down(), casted);
					
					casted.clientSideSpikesAnimationO = casted.clientSideSpikesAnimation;
		            casted.clientSideSpikesAnimation += (1.0F - casted.clientSideSpikesAnimation) * 0.06F;
		            
		            casted.clientSideTailAnimationO = casted.clientSideTailAnimation;
		            
		            float tailAnimationSpeed = casted.isInWater() ? 0.4f : 0.2f;
		            
		            if(event.player.getMotion().lengthSquared() > 0.01f)
		            	tailAnimationSpeed *= 3.5;
		            
		            casted.clientSideTailAnimation += tailAnimationSpeed;
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRenderer(RenderGameOverlayEvent.Post event)
	{
		if(event.getType() != ElementType.PORTAL)
			return;
		
		IGuardianBeamCapability beamCap = Minecraft.getInstance().player.getCapability(GuardianBeamCapabilityAttacher.GUARDIAN_BEAM_CAP).resolve().orElse(null);
		
		// Render beam
		if(beamCap != null && beamCap.getAttackedEntity().isPresent())
		{			
		    RenderSystem.depthMask(false);

			float progression = beamCap.getAttackProgression() + event.getPartialTicks();
			float linearProgression = progression / beamCap.getMaxAttackProgression();
			float progressionSq = linearProgression * linearProgression;
			float animationSpeed = .025f;
			
			float animationMovement = (progression) * animationSpeed;
			animationMovement *= animationMovement;
			
			Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("textures/entity/guardian_beam.png"));
			RenderSystem.enableBlend();
			float scale = 2.5f;
			
            float r = 0.25f + (progressionSq * 0.75f);
	        float g = 0.125f + (progressionSq * 0.75f);
	        float b = 0.5f - (progressionSq * 0.25f);
						
			renderColoredRect(event.getMatrixStack(), 0, 0, Minecraft.getInstance().getMainWindow().getScaledWidth(),
					Minecraft.getInstance().getMainWindow().getScaledHeight(), animationMovement, animationMovement, scale, scale, r, g, b,
					(float) Math.abs(Math.sin(Math.pow(progression, 1.2f) / 7f)) * (progression) / 100);
			
		      RenderSystem.depthMask(true);
		}
	}
	
	private static void renderColoredRect(MatrixStack matrix, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight, float r, float g, float b, float a)
	{
		float ar = width / ((float)height);
		
		renderColoredRect(matrix, x, x + width, y, y + height, 0, u, u + (uWidth) * ar, v, v + vHeight, r, g, b, a);
	}
	
	private static void renderColoredRect(MatrixStack matrix, float xMin, float xMax, float yMin, float yMax, float zIndex, float minU, float maxU, float minV, float maxV, float r, float g, float b, float a)
	{
	      BufferBuilder bb = Tessellator.getInstance().getBuffer();
	      bb.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
	      bb.pos(matrix.getLast().getMatrix(), xMin, yMax, zIndex).color(r, g, b, a).tex(minU, maxV).endVertex();
	      bb.pos(matrix.getLast().getMatrix(), xMax, yMax, zIndex).color(r, g, b, a).tex(maxU, maxV).endVertex();
	      bb.pos(matrix.getLast().getMatrix(), xMax, yMin, zIndex).color(r, g, b, a).tex(maxU, minV).endVertex();
	      bb.pos(matrix.getLast().getMatrix(), xMin, yMin, zIndex).color(r, g, b, a).tex(minU, minV).endVertex();
	      bb.finishDrawing();
	      WorldVertexBufferUploader.draw(bb);
	}
}
