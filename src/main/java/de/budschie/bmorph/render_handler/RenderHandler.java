package de.budschie.bmorph.render_handler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import de.budschie.bmorph.api_interact.ShrinkAPIInteractor;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.AdvancedAbstractClientPlayerEntity;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer.Impl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler
{	
	// this is not a good idea.
	public static final MorphFirstPersonRenderer FPR = new MorphFirstPersonRenderer(Minecraft.getInstance());
	
	public static WeakHashMap<UUID, Entity> cachedEntities = new WeakHashMap<>();
	
	@SubscribeEvent
	public static void onRenderHandEvent(RenderHandEvent event)
	{
		LazyOptional<IMorphCapability> morph = Minecraft.getInstance().player
				.getCapability(MorphCapabilityAttacher.MORPH_CAP);

		if (morph.isPresent())
		{
			event.setCanceled(true);

			Optional<MorphItem> currentMorph = morph.resolve().get().getCurrentMorph();

			if (currentMorph.isPresent())
			{
				event.setCanceled(true);

				PlayerEntity player = Minecraft.getInstance().player;

				checkCache(player.getUniqueID(), morph.resolve().get(), player);

				Entity toRender = cachedEntities.get(player.getUniqueID());

				EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager()
						.getRenderer(toRender);

				if (manager instanceof BipedRenderer<?, ?>)
				{
//					BipedModel<?> model = ((BipedRenderer<?, ?>)manager).entityModel;
//					
//					event.getMatrixStack().push();
//					//Minecraft.getInstance().getFirstPersonRenderer().transformFirstPerson(event.getMatrixStack(), player.getPrimaryHand(), player.getSwingProgress(event.getPartialTicks()));
//					// Minecraft.getInstance().getFirstPersonRenderer().renderItemInFirstPerson(0, null, null, null, 0);
//					event.getMatrixStack().rotate(Vector3f.YP.rotationDegrees(92.0F));
//					event.getMatrixStack().rotate(Vector3f.XP.rotationDegrees(45.0F));
//					event.getMatrixStack().rotate(Vector3f.ZP.rotationDegrees(-1 * -41.0F));
//					event.getMatrixStack().translate((double) (1 * 0.3F), (double) -1.1F, (double) 0.45F);
//					model.bipedLeftArm.render(event.getMatrixStack(), event.getBuffers().getBuffer(RenderType.getEntityCutout(manager.getEntityTexture(toRender))), event.getLight(), event.getLight());
//					event.getMatrixStack().pop();

					FPR.setModel(Optional.of((BipedRenderer) manager));
				} else
					FPR.setModel(Optional.empty());

				// TODO: Move this tick in client tick
				FPR.tick();
				
				event.getMatrixStack().push();

				FPR.renderItemInFirstPerson(event.getPartialTicks(), event.getMatrixStack(),
						Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(),
						Minecraft.getInstance().player, event.getLight());
				
				event.getMatrixStack().pop();
			}
		}
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
	
	private static void checkCache(UUID playerUUID, IMorphCapability capability, PlayerEntity player)
	{
		// Check if the entity is cached or if it should be updated
		if(cachedEntities.get(playerUUID) == null || capability.isDirty())
		{
			Entity toCache = capability.getCurrentMorph().get().createEntity(player.world);
			cachedEntities.put(player.getUniqueID(), toCache);
			capability.cleanDirty();
								
			MinecraftForge.EVENT_BUS.post(new InitializeMorphEntityEvent(player, toCache));
		}
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
				
				checkCache(player.getUniqueID(), morph.resolve().get(), player);
				
				Entity toRender = cachedEntities.get(player.getUniqueID());
				
				if(toRender == null || morph.resolve().get().isDirty())
				{
				}
				
				if(toRender.world != player.world)
				{
					toRender.setWorld(toRender.world);
				}
				
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
				
				toRender.rotationYaw = player.rotationYaw;
				toRender.prevRotationYaw = player.prevRotationYaw;
				
				float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
				
				event.getMatrixStack().push();
				
				if(ShrinkAPIInteractor.getInteractor().isShrunk(player))
					event.getMatrixStack().scale(0.81f / divisor, 0.81f / divisor, 0.81f / divisor);
				
				if(player.isCrouching() && ShrinkAPIInteractor.getInteractor().isShrunk(player))
					event.getMatrixStack().translate(0, 1, 0);
							
				
				// info: We are getting NOTEX when displaying tVariant render thingys by better animals plus https://github.com/itsmeow/betteranimalsplus/blob/1.16/src/main/java/its_meow/betteranimalsplus/client/ClientLifecycleHandler.java
				// NOTE: This does not occur when using tSingle...
				EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager().getRenderer(toRender);
				//System.out.println(texture);
				manager.render(toRender, 0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
				
				event.getMatrixStack().pop();
			}
		}
	}
}
