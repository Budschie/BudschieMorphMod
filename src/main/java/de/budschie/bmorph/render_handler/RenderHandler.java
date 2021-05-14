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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class RenderHandler
{
	public static WeakHashMap<UUID, Entity> cachedEntities = new WeakHashMap<>();
	
	@SubscribeEvent
	public static void onRenderHandEvent(RenderHandEvent event)
	{
		@SuppressWarnings("resource")
		LazyOptional<IMorphCapability> morph = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(morph.isPresent())
		{
			Optional<MorphItem> currentMorph = morph.resolve().get().getCurrentMorph();
			
			if(currentMorph.isPresent())
			{
				event.setCanceled(true);
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
	
	@SubscribeEvent
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
				
				if(toRender == null || morph.resolve().get().isDirty())
				{
					toRender = currentMorph.get().createEntity(player.world);
					cachedEntities.put(player.getUniqueID(), toRender);
					morph.resolve().get().cleanDirty();
										
					MinecraftForge.EVENT_BUS.post(new InitializeMorphEntityEvent(player, toRender));
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
				
				float divisor = ShrinkAPIInteractor.getInteractor().getShrinkingValue(player);
				
				event.getMatrixStack().push();
				
				if(ShrinkAPIInteractor.getInteractor().isShrunk(player))
					event.getMatrixStack().scale(0.81f / divisor, 0.81f / divisor, 0.81f / divisor);
				
				if(player.isCrouching() && ShrinkAPIInteractor.getInteractor().isShrunk(player))
					event.getMatrixStack().translate(0, 1, 0);
				
				EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager().getRenderer(toRender);
				manager.render(toRender, 0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
				
				event.getMatrixStack().pop();
			}
		}
	}
}
