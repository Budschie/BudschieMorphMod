package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RideAbility extends Ability
{
	public static final Codec<RideAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ModCodecs.PREDICATE.listOf().listOf().fieldOf("predicates").forGetter(RideAbility::getPredicates))
			.apply(instance, RideAbility::new));
	
	private List<List<LazyRegistryWrapper<LootItemCondition>>> predicates;
	
	public RideAbility(List<List<LazyRegistryWrapper<LootItemCondition>>> predicates)
	{
		this.predicates = predicates;
	}
	
	@SubscribeEvent
	public void onInteractWithEntity(EntityInteract event)
	{
		if(!event.getEntity().level.isClientSide() && isTracked(event.getEntity()))
		{
			executePredicateTest(event.getPlayer(), event.getTarget(), () ->
			{
				event.getPlayer().startRiding(event.getTarget());
				event.getPlayer().setYRot(event.getTarget().getYRot());
				event.getPlayer().setXRot(event.getTarget().getXRot());
			});
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	@Override
	public void disableAbility(Player player, MorphItem disabledItem, MorphItem newMorph, List<Ability> newAbilities, AbilityChangeReason reason)
	{
		if(!player.level.isClientSide())
		{
			executePredicateTest(player, player.getVehicle(), () ->
			{
				player.stopRiding();
			});
		}
				
		super.disableAbility(player, disabledItem, newMorph, newAbilities, reason);
	}
	
	private void executePredicateTest(Player player, Entity ridingEntity, Runnable onSuccess)
	{
		if(!player.level.isClientSide())
		{
			if(ridingEntity != null)
			{
				LootItemCondition[][] lootItemConditions = BudschieUtils.resolveConditions(predicates);
				
				LootContext.Builder predicateContext = (new LootContext.Builder((ServerLevel)ridingEntity.getLevel())).withParameter(LootContextParams.ORIGIN, ridingEntity.position())
						.withOptionalParameter(LootContextParams.THIS_ENTITY, ridingEntity);
				
				boolean predicateTrue = BudschieUtils.testPredicates(lootItemConditions, () -> predicateContext.create(LootContextParamSets.COMMAND));
				
				if(predicateTrue)
				{
					onSuccess.run();
				}
			}
		}
	}
	
	public List<List<LazyRegistryWrapper<LootItemCondition>>> getPredicates()
	{
		return predicates;
	}
}
