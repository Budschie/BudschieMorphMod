package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.util.ProtectedMethodAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class LivingEntitySynchronzier implements IEntitySynchronizer
{
	private static final ProtectedMethodAccess<LivingEntity, Void> SET_LIVING_ENTITY_FLAGS = new ProtectedMethodAccess<>(LivingEntity.class, "m_21155_", Integer.TYPE, Boolean.TYPE);
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof LivingEntity;
	}
	
	@Override
	public void applyToMorphEntityPostTick(Entity morphEntity, Player player)
	{
		LivingEntity entity = (LivingEntity) morphEntity;

		entity.walkDist = player.walkDist;
		entity.walkDistO = player.walkDistO;
		
		entity.attackAnim = player.attackAnim;
		entity.oAttackAnim = player.oAttackAnim;
		
		entity.yBodyRot = player.yBodyRot;
		entity.yBodyRotO = player.yBodyRotO;
		entity.yHeadRot = player.yHeadRot;
		entity.yHeadRotO = player.yHeadRotO;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		LivingEntity entity = (LivingEntity) morphEntity;
		
		
//		entity.animationPosition = player.animationPosition;
//		entity.animationSpeed = player.animationSpeed;
//		entity.animationSpeedOld = player.animationSpeedOld;
		
		entity.deathTime = player.deathTime;
		
		entity.hurtTime = player.hurtTime;
		entity.hurtMarked = player.hurtMarked;
		
		entity.swinging = player.swinging;
		entity.swingTime = player.swingTime;
				
		entity.setInvisible(player.isInvisible());
		
		entity.blocksBuilding = player.blocksBuilding;
				
		if(player.getSleepingPos().isPresent())
			entity.setSleepingPos(player.getSleepingPos().get());
				
		// More WTF?!? Btw if you are asking yourself "WTF?!?", this is because else, there is some weird shit going on with hands and stuff.
		// But this could just be me being stupid
		
//		if(entity instanceof AbstractSkeleton || entity instanceof Player)
//		{
//			entity.setItemInHand(InteractionHand.OFF_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
//			entity.setItemInHand(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.OFF_HAND));
//		}
//		else
//		{
			entity.setItemInHand(InteractionHand.MAIN_HAND, player.getItemInHand(InteractionHand.MAIN_HAND));
			entity.setItemInHand(InteractionHand.OFF_HAND, player.getItemInHand(InteractionHand.OFF_HAND));
//		}
		
//		entity.setItemStackToSlot(entity.getPrimaryHand() == HandSide.LEFT ? EquipmentSlotType.OFFHAND : EquipmentSlotType.MAINHAND, player.getItemStackFromSlot(EquipmentSlotType.MAINHAND));
//		entity.setItemStackToSlot(entity.getPrimaryHand() == HandSide.LEFT ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND, player.getItemStackFromSlot(EquipmentSlotType.OFFHAND));
		entity.setItemSlot(EquipmentSlot.FEET, player.getItemBySlot(EquipmentSlot.FEET));
		entity.setItemSlot(EquipmentSlot.LEGS, player.getItemBySlot(EquipmentSlot.LEGS));
		entity.setItemSlot(EquipmentSlot.CHEST, player.getItemBySlot(EquipmentSlot.CHEST));
		entity.setItemSlot(EquipmentSlot.HEAD, player.getItemBySlot(EquipmentSlot.HEAD));
		
		entity.fallFlyTicks = player.getFallFlyingTicks();
		
		entity.setPose(player.getPose());
		entity.setSwimming(player.isSwimming());
		
		entity.setDeltaMovement(player.getDeltaMovement());
		
		entity.setHealth(player.getHealth());
		
		if(entity.isUsingItem() != player.isUsingItem())
		{
			if(entity.isUsingItem())
			{
				entity.stopUsingItem();
				SET_LIVING_ENTITY_FLAGS.getValue(entity, 1, false);
			}
			else
			{
				entity.startUsingItem(player.getUsedItemHand());
				SET_LIVING_ENTITY_FLAGS.getValue(entity, 1, true);
				SET_LIVING_ENTITY_FLAGS.getValue(entity, 2, player.getUsedItemHand() == InteractionHand.OFF_HAND);
			}
		}
	}

}
