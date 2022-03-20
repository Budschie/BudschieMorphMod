package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ImmuneToDamageIfAbility extends Ability
{
	public static final Codec<ImmuneToDamageIfAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ModCodecs.PREDICATE.listOf().listOf().fieldOf("predicates").forGetter(ImmuneToDamageIfAbility::getPredicates)
			).apply(instance, ImmuneToDamageIfAbility::new));
	
	private List<List<LazyRegistryWrapper<LootItemCondition>>> predicates;
	
	public ImmuneToDamageIfAbility(List<List<LazyRegistryWrapper<LootItemCondition>>> predicates)
	{
		this.predicates = predicates;
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	@SubscribeEvent
	public void onPlayerAttacked(LivingAttackEvent event)
	{		
		if(event.getEntity().level.isClientSide() || !isTracked(event.getEntity()))
			return;
		
		Player player = (Player) event.getEntity();
		
		LootItemCondition[][] resolved = BudschieUtils.resolveConditions(predicates);
		
		LootContext.Builder predicateContext = (new LootContext.Builder((ServerLevel)player.level)).withParameter(LootContextParams.ORIGIN, player.position()).withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
				.withOptionalParameter(LootContextParams.THIS_ENTITY, player);
		
		if(BudschieUtils.testPredicates(resolved, () -> predicateContext.create(LootContextParamSets.ENTITY)))
		{
			event.setCanceled(true);
		}
	}
	
	public List<List<LazyRegistryWrapper<LootItemCondition>>> getPredicates()
	{
		return predicates;
	}
}
