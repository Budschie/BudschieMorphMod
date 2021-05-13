package de.budschie.bmorph.render_handler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.AdvancedAbstractClientPlayerEntity;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
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
	
	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void onRenderedHandler(RenderPlayerEvent event)
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
					
					if(event.getPlayer() == Minecraft.getInstance().player)
						toRender.setCustomNameVisible(false);
					
					if(toRender instanceof AbstractClientPlayerEntity)
					{
						AbstractClientPlayerEntity entity = (AbstractClientPlayerEntity) toRender;
						
						// WTF?!?
						entity.setPrimaryHand(player.getPrimaryHand() == HandSide.LEFT ? HandSide.RIGHT : HandSide.LEFT);
					}
					
					if(toRender instanceof AdvancedAbstractClientPlayerEntity)
					{
						AdvancedAbstractClientPlayerEntity advanced = (AdvancedAbstractClientPlayerEntity) toRender;
						
						// I LOVE lambdas!
						advanced.setIsWearing(part -> player.isWearing(part));
					}
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
				
				EntityRenderer<? super Entity> manager = Minecraft.getInstance().getRenderManager().getRenderer(toRender);
				
				manager.render(toRender, 0, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
			}
		}
	}
}
