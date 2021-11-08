package de.budschie.bmorph.morph.functionality.configurable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.StunAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedList;

/** perfect name i really nailed it and its so short -nobody **/
public class RandomDelegatingOnUseAbility extends StunAbility
{
	private static Logger LOGGER = LogManager.getLogger();
	
	public static final Codec<RandomDelegatingOnUseAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("stun").forGetter(RandomDelegatingOnUseAbility::getStun),
			WeightedList.getCodec(ResourceLocation.CODEC).fieldOf("abilities").forGetter(RandomDelegatingOnUseAbility::getWeightedList))
			.apply(instance, RandomDelegatingOnUseAbility::new));
	
	private WeightedList<ResourceLocation> weightedList;
	
	public RandomDelegatingOnUseAbility(int stun, WeightedList<ResourceLocation> weightedList)
	{
		super(stun);
		
		this.weightedList = weightedList;
	}
	
	public WeightedList<ResourceLocation> getWeightedList()
	{
		return weightedList;
	}
	
	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUniqueID()))
		{
			stun(player.getUniqueID());
			
			ResourceLocation randomRL = weightedList.getRandomValue(player.getEntityWorld().getRandom());
			
			Ability ability = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getAbility(randomRL);
			
			if(ability == null)
			{
				LOGGER.warn(String.format("The random delegating ability %s tried to delegate its work to %s, but that ability doesn't exist.", this.getResourceLocation(), randomRL));
			}
			else
			{
				ability.onUsedAbility(player, currentMorph);
			}
		}
	}
}
