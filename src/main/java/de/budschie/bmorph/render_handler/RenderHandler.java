package de.budschie.bmorph.render_handler;

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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;
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
				
				// I should soon clear this code up
				if(toRender instanceof AbstractClientPlayerEntity)
				{
					AbstractClientPlayerEntity entity = (AbstractClientPlayerEntity) toRender;
					
					entity.chasingPosX = player.chasingPosX;
					entity.prevChasingPosX = player.prevChasingPosX;
					entity.chasingPosY = player.chasingPosY;
					entity.prevChasingPosY = player.prevChasingPosY;
					entity.chasingPosZ = player.chasingPosZ;
					entity.prevChasingPosZ = player.prevChasingPosZ;		
					
					
					if(entity.isElytraFlying() != player.isElytraFlying())
					{
						if(player.isElytraFlying())
							entity.startFallFlying();
						else
							entity.stopFallFlying();
					}
				}
				
				if(toRender instanceof LivingEntity)
				{
					LivingEntity entity = (LivingEntity) toRender;
					entity.renderYawOffset = event.getPlayer().prevRenderYawOffset;
					entity.renderYawOffset = player.renderYawOffset;
					entity.prevRenderYawOffset = player.prevRenderYawOffset;
					entity.rotationYawHead = player.rotationYawHead;
					entity.prevRotationYawHead = player.prevRotationYawHead;
					
					entity.rotationPitch = player.rotationPitch;
					entity.prevRotationPitch = player.prevRotationPitch;
					
					entity.distanceWalkedModified = player.distanceWalkedModified;
					entity.prevDistanceWalkedModified = player.prevDistanceWalkedModified;
					
					entity.limbSwing = player.limbSwing;
					entity.limbSwingAmount = player.limbSwingAmount;
					entity.prevLimbSwingAmount = player.prevLimbSwingAmount;
					
					entity.deathTime = player.deathTime;
					
					entity.hurtTime = player.hurtTime;
					entity.velocityChanged = player.velocityChanged;
					
					entity.isSwingInProgress = player.isSwingInProgress;
					entity.swingProgressInt = player.swingProgressInt;
					
					entity.swingProgress = player.swingProgress;
					entity.prevSwingProgress = player.prevSwingProgress;
					
					entity.setInvisible(player.isInvisible());
					
					entity.preventEntitySpawning = player.preventEntitySpawning;
					
					// More WTF?!? Btw if you are asking yourself "WFT?!?", this is because else, there is some weird shit going on with hands and stuff.
					entity.setItemStackToSlot(EquipmentSlotType.MAINHAND, player.getItemStackFromSlot(EquipmentSlotType.OFFHAND));
					entity.setItemStackToSlot(EquipmentSlotType.OFFHAND, player.getItemStackFromSlot(EquipmentSlotType.MAINHAND));
					entity.setItemStackToSlot(EquipmentSlotType.FEET, player.getItemStackFromSlot(EquipmentSlotType.FEET));
					entity.setItemStackToSlot(EquipmentSlotType.LEGS, player.getItemStackFromSlot(EquipmentSlotType.LEGS));
					entity.setItemStackToSlot(EquipmentSlotType.CHEST, player.getItemStackFromSlot(EquipmentSlotType.CHEST));
					entity.setItemStackToSlot(EquipmentSlotType.HEAD, player.getItemStackFromSlot(EquipmentSlotType.HEAD));
					
					entity.ticksElytraFlying = player.getTicksElytraFlying();
					
					entity.setPose(player.getPose());
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
