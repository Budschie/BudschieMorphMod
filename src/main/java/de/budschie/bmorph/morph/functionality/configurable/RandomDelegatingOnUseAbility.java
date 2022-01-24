package de.budschie.bmorph.morph.functionality.configurable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.StunAbility;
import de.budschie.bmorph.util.WeightedList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** perfect name i really nailed it and its so short -nobody **/
public class RandomDelegatingOnUseAbility extends StunAbility
{
	private static Logger LOGGER = LogManager.getLogger();
	
	public static final Codec<RandomDelegatingOnUseAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("stun").forGetter(RandomDelegatingOnUseAbility::getStun),
			WeightedList.codecOf(ResourceLocation.CODEC).fieldOf("abilities").forGetter(RandomDelegatingOnUseAbility::getWeightedList))
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
	public void onUsedAbility(Player player, MorphItem currentMorph)
	{
		if(!isCurrentlyStunned(player.getUUID()))
		{
			stun(player.getUUID());
			
			ResourceLocation randomRL = weightedList.getRandom(player.getCommandSenderWorld().getRandom());
			
			Ability ability = BMorphMod.DYNAMIC_ABILITY_REGISTRY.getEntry(randomRL);
			
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
