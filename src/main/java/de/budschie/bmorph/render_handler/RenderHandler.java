package de.budschie.bmorph.render_handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.player.AdvancedAbstractClientPlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
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
	
	public static Entity getCachedEntity(PlayerEntity player)
	{
		return getCachedEntity(player.getUniqueID());
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
		
		if(event.getMorphEntity() instanceof AbstractClientPlayerEntity)
		{
			AbstractClientPlayerEntity entity = (AbstractClientPlayerEntity) event.getMorphEntity();
			
			// WTF?!?
			entity.setPrimaryHand(event.getPlayer().getPrimaryHand() == HandSide.LEFT ? HandSide.RIGHT : HandSide.LEFT);
		}
		
		if(event.getMorphEntity() instanceof AdvancedAbstractClientPlayerEntity)
		{
			AdvancedAbstractClientPlayerEntity advanced = (AdvancedAbstractClientPlayerEntity) event.getMorphEntity();
			
			// I LOVE lambdas!
			advanced.setIsWearing(part -> event.getPlayer().isWearing(part));
		}
		
		if(event.getMorphEntity() instanceof MobEntity)
		{
			if(event.getMorphEntity() instanceof AbstractSkeletonEntity)
				((MobEntity)event.getMorphEntity()).setLeftHanded(event.getPlayer().getPrimaryHand() == HandSide.RIGHT);
			else
				((MobEntity)event.getMorphEntity()).setLeftHanded(event.getPlayer().getPrimaryHand() == HandSide.LEFT);
		}
	}
	
	public static void disposePlayerMorphData(UUID player)
	{
		handleCacheRemoval(cachedEntities.get(player));
		cachedEntities.remove(player);
	}
	
	public static void onBuildNewEntity(PlayerEntity player, IMorphCapability capability, MorphItem aboutToMorphTo)
	{
		if(aboutToMorphTo == null)
			handleCacheRemoval(cachedEntities.remove(player.getUniqueID()));
		else
		{
			Entity toCreate = aboutToMorphTo.createEntity(player.world);
			
			// Remove the previous entity
			handleCacheRemoval(cachedEntities.get(player.getUniqueID()));
			cachedEntities.put(player.getUniqueID(), toCreate);
			
			MinecraftForge.EVENT_BUS.post(new InitializeMorphEntityEvent(player, toCreate));
		}
	}
	
	// This method calls remove on the previous entity. We have to do this as this might create a dangling reference
	// when playing the GuardianSound otherwise.
	private static void handleCacheRemoval(Entity entity)
	{
		if(entity != null)
			entity.remove();
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

				PlayerEntity player = event.getPlayer();
				
				Entity toRender = cachedEntities.get(player.getUniqueID());
				
				renderMorph(player, toRender, event.getMatrixStack(), event.getPartialRenderTick(), event.getBuffers(), event.getLight());
			}
		}
	}
	
	public static void renderMorph(PlayerEntity player, Entity toRender, MatrixStack matrixStack, float partialRenderTicks, IRenderTypeBuffer buffers, int light)
	{
		if(toRender.world != player.world)
		{
			toRender.setWorld(toRender.world);
		}
		
		// Holy shit that's unperformant... I'll maybe change this later
		ArrayList<IEntitySynchronizer> list = EntitySynchronizerRegistry.getSynchronizers();
		
		for(IEntitySynchronizer sync : list)
		{
			if(sync.appliesToMorph(toRender))
				sync.applyToMorphEntity(toRender, player);
		}
		
		toRender.ticksExisted = player.ticksExisted;
		
		toRender.setPosition(player.getPosX(), player.getPosY(), player.getPosZ());
		
		toRender.prevPosX = player.prevPosX;
		toRender.prevPosY = player.prevPosY;
		toRender.prevPosZ = player.prevPosZ;
		
		toRender.rotationPitch = player.rotationPitch;
		toRender.rotationYaw = player.rotationYaw;
		toRender.rotationPitch = player.rotationPitch;
		toRender.prevRotationPitch = player.prevRotationPitch;
		
		toRender.lastTickPosX = player.lastTickPosX;
		toRender.lastTickPosY = player.lastTickPosY;
		toRender.lastTickPosZ = player.lastTickPosZ;
		
		toRender.rotationYaw = player.rotationYaw;
		toRender.prevRotationYaw = player.prevRotationYaw;
		
		toRender.inWater = player.isInWater();
		
		float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
		
		matrixStack.push();
		
		if(ShrinkAPIInteractor.getInteractor().isShrunk(player))
			matrixStack.scale(0.81f / divisor, 0.81f / divisor, 0.81f / divisor);
		
		if(player.isCrouching() && ShrinkAPIInteractor.getInteractor().isShrunk(player))
			matrixStack.translate(0, 1, 0);
					
		
		// info: We are getting NOTEX when displaying tVariant render thingys by better animals plus https://github.com/itsmeow/betteranimalsplus/blob/1.16/src/main/java/its_meow/betteranimalsplus/client/ClientLifecycleHandler.java
		// NOTE: This does not occur when using tSingle...
		EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager().getRenderer(toRender);
		//System.out.println(texture);
		manager.render(toRender, 0, partialRenderTicks, matrixStack, buffers, light);
		
		matrixStack.pop();
	}
}
