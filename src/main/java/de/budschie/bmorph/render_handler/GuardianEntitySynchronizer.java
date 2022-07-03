package de.budschie.bmorph.render_handler;

import com.mojang.blaze3d.systems.RenderSystem;

import de.budschie.bmorph.capabilities.guardian.GuardianBeamCapabilityInstance;
import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// We do this shit instead of using an IEntitySynchronizer because we have to sync data with the cached entity, even if it is not rendered, as the synced data is used for the GuardianSound instance.
@EventBusSubscriber(value = Dist.CLIENT)
public class GuardianEntitySynchronizer implements IEntitySynchronizer
{	
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Guardian;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		IGuardianBeamCapability beamCap = player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).resolve().orElse(null);

		if (beamCap != null)
		{
			Guardian casted = (Guardian) morphEntity;

			casted.clientSideAttackTime = beamCap.getAttackProgression();

			if (beamCap.getAttackedEntity().isPresent())
			{
				casted.setActiveAttackTarget(beamCap.getAttackedEntity().get());
			} 
			else
			{
				casted.setActiveAttackTarget(0);
			}
		}
	}
	
	// Render the UI when shooting lasers
	@SubscribeEvent
	public static void onPlayerRenderer(RenderGameOverlayEvent.Pre event)
	{
		if(event.getType() != ElementType.TEXT)
			return;
		
		IGuardianBeamCapability beamCap = Minecraft.getInstance().player.getCapability(GuardianBeamCapabilityInstance.GUARDIAN_BEAM_CAP).resolve().orElse(null);
		
		// Render beam
		if(beamCap != null && beamCap.getAttackedEntity().isPresent())
		{
			float progression = beamCap.getAttackProgression() + event.getPartialTicks();
			float linearProgression = progression / beamCap.getMaxAttackProgression();
			float progressionSq = linearProgression * linearProgression;
			float animationSpeed = .25f;
			
			float animationMovement = (progression) * animationSpeed;
			animationMovement *= animationMovement;
			
			RenderSystem.setShaderTexture(0, new ResourceLocation("textures/entity/guardian_beam.png"));
			RenderSystem.enableBlend();
			
			float scale = 7f;
			
            float r = 0.25f + (progressionSq * 0.75f);
	        float g = 0.125f + (progressionSq * 0.75f);
	        float b = 0.5f - (progressionSq * 0.25f);
	        float a = (float) Math.abs(Math.sin(Math.pow(progression / 2.0f, 1.2f) / 7f)) * (progression) / 100;
	        
			RenderSystem.setShaderColor(r, g, b, a);
	        
//			renderColoredRect(event.getMatrixStack(), 0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(),
//					Minecraft.getInstance().getWindow().getGuiScaledHeight(), animationMovement, animationMovement, scale, scale, r, g, b,
//					a);
			
	        Gui.blit(event.getMatrixStack(), 0, 0, animationMovement, animationMovement, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), (int)(32 * scale), (int)(32 * scale));
	        
			RenderSystem.disableBlend();
		}
	}
}
