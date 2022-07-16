package de.budschie.bmorph.morph.functionality.configurable;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.ModCodecs;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.util.LazyOptional;

/**
 * This ability will be used to only activate certain abilities under certain circumstances.
 * A concrete example of this would be that a small magma cube has x armor whilst a big magma cube has y armor.
 * 
 * @author budschie
 */
public class ConditionalAbility extends Ability
{
	public static final Codec<ConditionalAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ModCodecs.PREDICATE.listOf().listOf().fieldOf("predicates").forGetter(ConditionalAbility::getPredicates),
				ModCodecs.ABILITY.listOf().fieldOf("abilities").forGetter(ConditionalAbility::getAbilities)
			).apply(instance, ConditionalAbility::new));
	
	// Predicate that the player should be tested against
	// Inner list OR outer list AND (like with advancements)
	private List<List<LazyRegistryWrapper<LootItemCondition>>> predicates;
	
	// Abilities that shall be executed when the predicate is true for the given amount of time
	private List<LazyOptional<Ability>> abilities;

	public ConditionalAbility(List<List<LazyRegistryWrapper<LootItemCondition>>> predicates, List<LazyOptional<Ability>> abilities)
	{
		this.predicates = predicates;
		this.abilities = abilities;
	}
	
	@Override
	public void enableAbility(Player player, MorphItem enabledItem, MorphItem oldMorph, List<Ability> oldAbilities, AbilityChangeReason reason)
	{
		super.enableAbility(player, enabledItem, oldMorph, oldAbilities, reason);
				
		if(!player.getLevel().isClientSide())
		{
			LootItemCondition[][] lootItemConditions = BudschieUtils.resolveConditions(predicates);
			
			LootContext.Builder predicateContext = (new LootContext.Builder((ServerLevel)player.level)).withParameter(LootContextParams.ORIGIN, player.position())
					.withOptionalParameter(LootContextParams.THIS_ENTITY, player);
			
			boolean predicateTrue = BudschieUtils.testPredicates(lootItemConditions, () -> predicateContext.create(LootContextParamSets.COMMAND));
			
			if(predicateTrue)
			{
				MorphUtil.processCap(player, cap ->
				{
					Ability[] list = new Ability[abilities.size()];
					
					int i = 0;
					
					for(LazyOptional<Ability> ability : abilities)
					{
						Ability resolved = ability.resolve().get();
						cap.applyAbility(resolved);
						list[i++] = resolved;
					}
					
					cap.syncAbilityAddition(list);
				});
			}
		}
	}
	
	public List<List<LazyRegistryWrapper<LootItemCondition>>> getPredicates()
	{
		return predicates;
	}
	
	public List<LazyOptional<Ability>> getAbilities()
	{
		return abilities;
	}
}
