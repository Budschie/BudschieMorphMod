package de.budschie.bmorph.render_handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.player.AdvancedAbstractClientPlayerEntity;
import de.budschie.bmorph.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler
{		
	private static HashMap<UUID, Entity> cachedEntities = new HashMap<>();
	
	public static Entity getCachedEntity(Player player)
	{
		return getCachedEntity(player.getUUID());
	}
	
	public static Entity getCachedEntity(UUID player)
	{
		return cachedEntities.get(player);
	}
	
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
	
	public static void disposePlayerMorphData(UUID player)
	{
		handleCacheRemoval(cachedEntities.get(player));
		cachedEntities.remove(player);
	}
	
	public static void onBuildNewEntity(Player player, IMorphCapability capability, MorphItem aboutToMorphTo)
	{
		if(aboutToMorphTo == null)
			handleCacheRemoval(cachedEntities.remove(player.getUUID()));
		else
		{
			Entity toCreate = aboutToMorphTo.createEntity(player.level);
			
			// Remove the previous entity
			handleCacheRemoval(cachedEntities.get(player.getUUID()));
			cachedEntities.put(player.getUUID(), toCreate);
			
			MinecraftForge.EVENT_BUS.post(new InitializeMorphEntityEvent(player, toCreate));
		}
	}
	
	// This method calls remove on the previous entity. We have to do this as this might create a dangling reference
	// when playing the GuardianSound otherwise.
	private static void handleCacheRemoval(Entity entity)
	{
		if(entity != null)
			entity.remove(RemovalReason.DISCARDED);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRenderedHandler(RenderPlayerEvent.Pre event)
	{
		LazyOptional<IMorphCapability> morph = event.getPlayer().getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(morph.isPresent())
		{
			Optional<MorphItem> currentMorph = morph.resolve().get().getCurrentMorph();
			
			if(currentMorph.isPresent())
			{
				event.setCanceled(true);

				Player player = event.getPlayer();
				
				Entity toRender = cachedEntities.get(player.getUUID());
				
				renderMorph(player, toRender, event.getMatrixStack(), event.getPartialRenderTick(), event.getBuffers(), event.getLight());
			}
		}
	}
	
	public static void renderMorph(Player player, Entity toRender, PoseStack matrixStack, float partialRenderTicks, MultiBufferSource buffers, int light)
	{
		if(toRender.level != player.level)
		{
			toRender.level = player.level;
		}
		
		// Holy shit that's unperformant... I'll maybe change this later
		ArrayList<IEntitySynchronizer> list = EntitySynchronizerRegistry.getSynchronizers();
				
		toRender.tickCount = player.tickCount;
		
		EntityUtil.copyLocationAndRotation(player, toRender);
		
		toRender.wasTouchingWater = player.isInWater();
		
		toRender.setOnGround(player.isOnGround());
		
		float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
		
		matrixStack.pushPose();
		
		if(ShrinkAPIInteractor.getInteractor().isShrunk(player))
			matrixStack.scale(0.81f / divisor, 0.81f / divisor, 0.81f / divisor);
		
		if(player.isCrouching() && ShrinkAPIInteractor.getInteractor().isShrunk(player))
			matrixStack.translate(0, 1, 0);
		
		for(IEntitySynchronizer sync : list)
		{
			if(sync.appliesToMorph(toRender))
				sync.applyToMorphEntity(toRender, player, partialRenderTicks);
		}
					
		
		// info: We are getting NOTEX when displaying tVariant render thingys by better animals plus https://github.com/itsmeow/betteranimalsplus/blob/1.16/src/main/java/its_meow/betteranimalsplus/client/ClientLifecycleHandler.java
		// NOTE: This does not occur when using tSingle...
		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(toRender);
		//System.out.println(texture);
		manager.render(toRender, 0, partialRenderTicks, matrixStack, buffers, light);
		
		matrixStack.popPose();
	}
}
