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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
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
		
		if(event.getMorphEntity() instanceof AbstractClientPlayer)
		{
			AbstractClientPlayer entity = (AbstractClientPlayer) event.getMorphEntity();
			
			// WTF?!?
			entity.setMainArm(event.getPlayer().getMainArm() == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
		}
		
		if(event.getMorphEntity() instanceof AdvancedAbstractClientPlayerEntity)
		{
			// What is this code that I produced??!? Plz send help...
			AdvancedAbstractClientPlayerEntity advanced = (AdvancedAbstractClientPlayerEntity) event.getMorphEntity();
			
			// I LOVE lambdas!
			advanced.setIsWearing(part -> event.getPlayer().isModelPartShown(part));
		}
		
		if(event.getMorphEntity() instanceof Mob)
		{
			if(event.getMorphEntity() instanceof AbstractSkeleton)
				((Mob)event.getMorphEntity()).setLeftHanded(event.getPlayer().getMainArm() == HumanoidArm.RIGHT);
			else
				((Mob)event.getMorphEntity()).setLeftHanded(event.getPlayer().getMainArm() == HumanoidArm.LEFT);
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
					
					if(toRender instanceof LivingEntity living)
					{
						living.yBodyRot = player.yBodyRot;
						living.setYRot(player.getYRot());
						living.setXRot(player.getXRot());
						living.yHeadRot = player.yHeadRot;
						living.yHeadRotO = player.yHeadRotO;
					}
					
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
				
				if(syncs != null)
				{
					syncs.forEach(sync -> sync.applyToMorphEntity(renderDataCapability.getOrCreateCachedEntity(event.player), event.player));
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
		
		// info: We are getting NOTEX when displaying tVariant render thingys by better animals plus https://github.com/itsmeow/betteranimalsplus/blob/1.16/src/main/java/its_meow/betteranimalsplus/client/ClientLifecycleHandler.java
		// NOTE: This does not occur when using tSingle...
		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(toRender);
		//System.out.println(texture);
		manager.render(toRender, 0, partialRenderTicks, matrixStack, buffers, light);
		
		matrixStack.popPose();
	}
}
