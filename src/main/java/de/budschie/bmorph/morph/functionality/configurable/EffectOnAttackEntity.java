package de.budschie.bmorph.morph.functionality.configurable;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Ability that gives entities effects when a player hurts them. **/
public class EffectOnAttackEntity extends Ability
{
	public static final Codec<EffectOnAttackEntity> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ModCodecs.EFFECT_INSTANCE.fieldOf("effect_instance").forGetter(EffectOnAttackEntity::getEffectInstance),
					ModCodecs.PREDICATE.listOf().listOf().optionalFieldOf("damage_predicates", Arrays.asList(Arrays.asList())).forGetter(EffectOnAttackEntity::getPredicates)).apply(instance, EffectOnAttackEntity::new));
	
	private MobEffectInstance effectInstance;
	private List<List<LazyRegistryWrapper<LootItemCondition>>> predicates;
	
	public EffectOnAttackEntity(MobEffectInstance effectInstance, List<List<LazyRegistryWrapper<LootItemCondition>>> predicates)
	{
		this.effectInstance = effectInstance;
		this.predicates = predicates;
	}
	
	public MobEffectInstance getEffectInstance()
	{
		return effectInstance;
	}
	
	@SubscribeEvent
	public void onEntityDamaged(LivingDamageEvent event)
	{
		if(event.getSource().getEntity() instanceof Player)
		{
			Player player = (Player) event.getSource().getEntity();
			
			if(isTracked(player))
			{
				LootItemCondition[][] lootItemConditions = BudschieUtils.resolveConditions(predicates);
				
				LootContext.Builder predicateContext = (new LootContext.Builder((ServerLevel)player.level)).withParameter(LootContextParams.ORIGIN, player.position())
						.withOptionalParameter(LootContextParams.THIS_ENTITY, event.getEntityLiving()).withOptionalParameter(LootContextParams.DAMAGE_SOURCE, event.getSource());
				
				boolean predicateTrue = BudschieUtils.testPredicates(lootItemConditions, () -> predicateContext.create(LootContextParamSets.ENTITY));
				
				if(predicateTrue)
				{
					event.getEntityLiving().addEffect(new MobEffectInstance(this.effectInstance));
				}
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
	
	public List<List<LazyRegistryWrapper<LootItemCondition>>> getPredicates()
	{
		return predicates;
	}
}
