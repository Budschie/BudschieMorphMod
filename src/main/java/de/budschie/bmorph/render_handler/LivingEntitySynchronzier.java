package de.budschie.bmorph.render_handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.Hand;

public class LivingEntitySynchronzier implements IEntitySynchronizer
{

	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof LivingEntity;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, PlayerEntity player)
	{
		LivingEntity entity = (LivingEntity) morphEntity;
		
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
		
		entity.renderYawOffset = player.prevRenderYawOffset;
		entity.renderYawOffset = player.renderYawOffset;
		entity.prevRenderYawOffset = player.prevRenderYawOffset;
		entity.rotationYawHead = player.rotationYawHead;
		entity.prevRotationYawHead = player.prevRotationYawHead;
		
		if(player.getBedPosition().isPresent())
			entity.setBedPosition(player.getBedPosition().get());
				
		// More WTF?!? Btw if you are asking yourself "WTF?!?", this is because else, there is some weird shit going on with hands and stuff.
		// But this could just be me being stupid
		
//		System.out.println("Player main hand: " + player.getPrimaryHand() + "; Entity main hand: " + entity.getPrimaryHand());
		
		if(entity instanceof AbstractSkeletonEntity || entity instanceof PlayerEntity)
		{
			entity.setHeldItem(Hand.OFF_HAND, player.getHeldItem(Hand.MAIN_HAND));
			entity.setHeldItem(Hand.MAIN_HAND, player.getHeldItem(Hand.OFF_HAND));
		}
		else
		{
			entity.setHeldItem(Hand.MAIN_HAND, player.getHeldItem(Hand.MAIN_HAND));
			entity.setHeldItem(Hand.OFF_HAND, player.getHeldItem(Hand.OFF_HAND));
		}
		
//		entity.setItemStackToSlot(entity.getPrimaryHand() == HandSide.LEFT ? EquipmentSlotType.OFFHAND : EquipmentSlotType.MAINHAND, player.getItemStackFromSlot(EquipmentSlotType.MAINHAND));
//		entity.setItemStackToSlot(entity.getPrimaryHand() == HandSide.LEFT ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND, player.getItemStackFromSlot(EquipmentSlotType.OFFHAND));
		entity.setItemStackToSlot(EquipmentSlotType.FEET, player.getItemStackFromSlot(EquipmentSlotType.FEET));
		entity.setItemStackToSlot(EquipmentSlotType.LEGS, player.getItemStackFromSlot(EquipmentSlotType.LEGS));
		entity.setItemStackToSlot(EquipmentSlotType.CHEST, player.getItemStackFromSlot(EquipmentSlotType.CHEST));
		entity.setItemStackToSlot(EquipmentSlotType.HEAD, player.getItemStackFromSlot(EquipmentSlotType.HEAD));
		
		entity.ticksElytraFlying = player.getTicksElytraFlying();
		
		entity.setPose(player.getPose());
		entity.setSwimming(player.isSwimming());
		
		entity.setMotion(player.getMotion());
	}

}
