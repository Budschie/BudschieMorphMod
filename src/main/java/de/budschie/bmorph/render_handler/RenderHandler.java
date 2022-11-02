package de.budschie.bmorph.render_handler;

import java.util.ArrayList;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.capabilities.client.render_data.IRenderDataCapability;
import de.budschie.bmorph.capabilities.client.render_data.RenderDataCapabilityProvider;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.player.AdvancedAbstractClientPlayerEntity;
import de.budschie.bmorph.tags.ModEntityTypeTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler
{
	private static boolean veryDodgyStackOverflowPreventionHackJesJes = false;
	
	// Setup the proxy entity when we initialize it
	@SubscribeEvent
	public static void onMorphInit(InitializeMorphEntityEvent event)
	{		
		if(event.getPlayer() == Minecraft.getInstance().player)
			event.getMorphEntity().setCustomNameVisible(false);
		
//		if(event.getMorphEntity() instanceof AbstractClientPlayer)
//		{
//			AbstractClientPlayer entity = (AbstractClientPlayer) event.getMorphEntity();
//			
//			// WTF?!?
//			entity.setMainArm(event.getPlayer().getMainArm() == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
//		}
		
		if(event.getMorphEntity() instanceof AdvancedAbstractClientPlayerEntity)
		{
			// What is this code that I produced??!? Plz send help...
			AdvancedAbstractClientPlayerEntity advanced = (AdvancedAbstractClientPlayerEntity) event.getMorphEntity();
			
			// I LOVE lambdas!
			advanced.setIsWearing(part -> event.getPlayer().isModelPartShown(part));
		}
		
		if(event.getMorphEntity() instanceof Mob mob)
		{
			mob.setLeftHanded(event.getPlayer().getMainArm() == HumanoidArm.LEFT);
		}
		
		if(event.getMorphEntity() instanceof LivingEntity living)
		{
			living.swingingArm = event.getPlayer().swingingArm;
		}
	}
	
	public static void onBuildNewEntity(Player player, IMorphCapability capability, MorphItem aboutToMorphTo)
	{
		IRenderDataCapability renderDataCapability = player.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().get();
		
		renderDataCapability.invalidateCache();
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRenderedHandler(RenderPlayerEvent.Pre event)
	{
		// TODO: Fix this mess EDIT: If I find a better way of doing this, I will implement it, but as of now, I wont
		if(veryDodgyStackOverflowPreventionHackJesJes)
			return;
		
		veryDodgyStackOverflowPreventionHackJesJes = true;
		IRenderDataCapability renderDataCapability = event.getPlayer().getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().orElse(null);
		
		if(renderDataCapability != null)
		{
			LazyOptional<IMorphCapability> morph = event.getPlayer().getCapability(MorphCapabilityAttacher.MORPH_CAP);
			
			if(renderDataCapability.hasAnimation())
			{
				renderDataCapability.renderAnimation(event.getPlayer(), event.getPoseStack(), event.getPartialTick(), event.getMultiBufferSource(), event.getPackedLight());
				event.setCanceled(true);
			}
			else if(morph.isPresent())
			{
				Optional<MorphItem> currentMorph = morph.resolve().get().getCurrentMorph();
				
				if(currentMorph.isPresent())
				{
					event.setCanceled(true);
	
					Player player = event.getPlayer();					
					Entity toRender = renderDataCapability.getOrCreateCachedEntity(player);
					
					renderDataCapability.getOrCreateCachedRotationSynchronizers(player).forEach(rotationSync -> rotationSync.updateMorphRotation(toRender, player));
					renderMorph(player, toRender, event.getPoseStack(), event.getPartialTick(), event.getMultiBufferSource(), event.getPackedLight());
				}
			}			
		}
		
		veryDodgyStackOverflowPreventionHackJesJes = false;
	}
	
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event)
	{
		if(event.side == LogicalSide.CLIENT && event.phase == Phase.END)
		{
			// Retrieve the player's IRenderDataCapability
			
			IRenderDataCapability renderDataCapability = event.player.getCapability(RenderDataCapabilityProvider.RENDER_CAP).resolve().orElse(null);
			
			if(renderDataCapability != null)
			{
				renderDataCapability.tickAnimation();
				
				ArrayList<IEntitySynchronizer> syncs = renderDataCapability.getOrCreateCachedSynchronizers(event.player);
				
				Entity entity = renderDataCapability.getOrCreateCachedEntity(event.player);
				
				// Apply all syncs.
				if(syncs != null)
				{
					syncs.forEach(sync -> sync.applyToMorphEntity(entity, event.player));
				}
				
				// Tick the entity
				
				if(entity != null)
				{
					entity.tick();
				}
				
				if(syncs != null)
				{
					syncs.forEach(sync -> sync.applyToMorphEntityPostTick(entity, event.player));
				}
			}
		}
	}
	
	public static void renderMorph(Player player, Entity toRender, PoseStack matrixStack, float partialRenderTicks, MultiBufferSource buffers, int light)
	{			
		float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
		
		matrixStack.pushPose();
		
		if(ShrinkAPIInteractor.getInteractor().isShrunk(player))
			matrixStack.scale(0.81f / divisor, 0.81f / divisor, 0.81f / divisor);
		
		if(toRender.isCrouching() && ShrinkAPIInteractor.getInteractor().isShrunk(player))
			matrixStack.translate(0, 1, 0);
		
		// If we are crouching and we should not move down, offset the player up again.
		if(player.isCrouching() && ForgeRegistries.ENTITIES.tags().getTag(ModEntityTypeTags.DISABLE_SNEAK_TRANSFORM).contains(toRender.getType()))
		{
			toRender.setPose(Pose.STANDING);
			matrixStack.translate(0, 0.125D, 0);
		}
		
		// info: We are getting NOTEX when displaying tVariant render thingys by better animals plus https://github.com/itsmeow/betteranimalsplus/blob/1.16/src/main/java/its_meow/betteranimalsplus/client/ClientLifecycleHandler.java
		// NOTE: This does not occur when using tSingle...
		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(toRender);
		manager.render(toRender, 0, partialRenderTicks, matrixStack, buffers, manager.getPackedLightCoords(toRender, partialRenderTicks));
		
		matrixStack.popPose();
	}
}
