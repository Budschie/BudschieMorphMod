package de.budschie.bmorph.render_handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityAttacher;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
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
				
				if(currentCachedEntity instanceof Guardian casted)
				{
					casted.clientSideAttackTime = beamCap.getAttackProgression();
					
					if(beamCap.getAttackedEntity().isPresent())
					{
						casted.setActiveAttackTarget(beamCap.getAttackedEntity().get());
					}
					else
					{
						casted.setActiveAttackTarget(0);
					}
					
					casted.clientSideTouchedGround = casted.getDeltaMovement().y < 0.0D && casted.level.loadedAndEntityCanStandOn(casted.blockPosition().below(), casted);
					
					casted.clientSideSpikesAnimationO = casted.clientSideSpikesAnimation;
		            casted.clientSideSpikesAnimation += (1.0F - casted.clientSideSpikesAnimation) * 0.06F;
		            
		            casted.clientSideTailAnimationO = casted.clientSideTailAnimation;
		            
		            float tailAnimationSpeed = casted.isInWater() ? 0.4f : 0.2f;
		            
		            if(event.player.getDeltaMovement().lengthSqr() > 0.01f)
		            	tailAnimationSpeed *= 3.5;
		            
		            casted.clientSideTailAnimation += tailAnimationSpeed;
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRenderer(RenderGameOverlayEvent.Post event)
	{
		if(event.getType() != ElementType.LAYER)
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
			
			RenderSystem.setShaderTexture(0, new ResourceLocation("textures/entity/guardian_beam.png"));
			RenderSystem.enableBlend();
			float scale = 2.5f;
			
            float r = 0.25f + (progressionSq * 0.75f);
	        float g = 0.125f + (progressionSq * 0.75f);
	        float b = 0.5f - (progressionSq * 0.25f);
						
			renderColoredRect(event.getMatrixStack(), 0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(),
					Minecraft.getInstance().getWindow().getGuiScaledHeight(), animationMovement, animationMovement, scale, scale, r, g, b,
					(float) Math.abs(Math.sin(Math.pow(progression, 1.2f) / 7f)) * (progression) / 100);
			
		      RenderSystem.depthMask(true);
		}
	}
	
	private static void renderColoredRect(PoseStack matrix, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight, float r, float g, float b, float a)
	{
		float ar = width / ((float)height);
		
		renderColoredRect(matrix, x, x + width, y, y + height, 0, u, u + (uWidth) * ar, v, v + vHeight, r, g, b, a);
	}
	
	private static void renderColoredRect(PoseStack matrix, float xMin, float xMax, float yMin, float yMax, float zIndex, float minU, float maxU, float minV, float maxV, float r, float g, float b, float a)
	{
	      BufferBuilder bb = Tesselator.getInstance().getBuilder();
	      bb.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX);
	      bb.vertex(matrix.last().pose(), xMin, yMax, zIndex).color(r, g, b, a).uv(minU, maxV).endVertex();
	      bb.vertex(matrix.last().pose(), xMax, yMax, zIndex).color(r, g, b, a).uv(maxU, maxV).endVertex();
	      bb.vertex(matrix.last().pose(), xMax, yMin, zIndex).color(r, g, b, a).uv(maxU, minV).endVertex();
	      bb.vertex(matrix.last().pose(), xMin, yMin, zIndex).color(r, g, b, a).uv(minU, minV).endVertex();
	      bb.end();
	      BufferUploader.end(bb);
	}
}
