package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.events.Events;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.morph.functionality.codec_addition.AudioVisualEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemStealAbility extends StunAbility
{
	public static final Codec<ItemStealAbility> CODEC = RecordCodecBuilder.create(builder ->
			builder.group(
					Codec.INT.fieldOf("stun").forGetter(ItemStealAbility::getStun),
					Codec.DOUBLE.fieldOf("reach").forGetter(ItemStealAbility::getReach),
					TagKey.codec(Registries.ENTITY_TYPE).optionalFieldOf("entity_blacklist").forGetter(ItemStealAbility::getEntityBlacklist),
					AudioVisualEffect.CODEC.optionalFieldOf("steal_effect").forGetter(ItemStealAbility::getStealEffect))
			.apply(builder, ItemStealAbility::new));
	
	private double reach;
	private Optional<TagKey<EntityType<?>>> entityBlacklist;
	private Optional<AudioVisualEffect> stealEffect;
	
	public ItemStealAbility(int stun, double reach, Optional<TagKey<EntityType<?>>> entityBlacklist, Optional<AudioVisualEffect> stealEffect)
	{
		super(stun);
		
		this.reach = reach;
		this.entityBlacklist = entityBlacklist;
		this.stealEffect = stealEffect;
	}

	@Override
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(isCurrentlyStunned(player.getUUID()))
		{
			return;
		}
		
		Vec3 from = player.position().add(Vec3.directionFromRotation(player.getRotationVector())).add(0, player.getEyeHeight(), 0);
		Vec3 to = Vec3.directionFromRotation(player.getRotationVector()).multiply(reach, reach, reach).add(from);
		
		Predicate<Entity> entityPredicate = (entity) -> true;
		
		if(entityBlacklist.isPresent())
		{
			entityPredicate = (entity) -> !ForgeRegistries.ENTITY_TYPES.tags().getTag(entityBlacklist.get()).contains(entity.getType());
		}
		
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, from, to, new AABB(from, to), entityPredicate, reach);
		
		boolean shouldStun = false;
		
		if(entityHit != null && entityHit.getEntity() != null)
		{
			shouldStun = switchItems(player, entityHit.getEntity());
		}
		
		if(shouldStun)
		{
			stun(player.getUUID());
			stealEffect.ifPresent(effect -> effect.playEffect(player));
			Events.aggro(player.getCapability(MorphCapabilityAttacher.MORPH_CAP).resolve().get(), getStun());
		}
	}
	
	private boolean switchItems(Player player, Entity entity)
	{
		// Jesus what the f
		SlotAccess slotAccessEntity = entity.getSlot(EquipmentSlot.MAINHAND.getIndex(98));
		
		if(slotAccessEntity != SlotAccess.NULL)
		{
			// Switching may commence
			ItemStack playerItemStack = player.getMainHandItem();
			ItemStack entityItemStack = slotAccessEntity.get();
			
			player.setItemSlot(EquipmentSlot.MAINHAND, entityItemStack);
			slotAccessEntity.set(playerItemStack);
			return true;
		}
		
		return false;
	}
	
	public Optional<AudioVisualEffect> getStealEffect()
	{
		return stealEffect;
	}
	
	public double getReach()
	{
		return reach;
	}
	
	public Optional<TagKey<EntityType<?>>> getEntityBlacklist()
	{
		return entityBlacklist;
	}
}
