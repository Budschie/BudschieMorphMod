package de.budschie.bmorph.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import de.budschie.bmorph.advancements.predicates.MorphPredicate;
import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class PlayerAttributesPredicate implements LootItemCondition
{
	private MinMaxBounds.Doubles playerHealth;
	private MorphPredicate morphPredicate;
	
	public PlayerAttributesPredicate(MinMaxBounds.Doubles playerHealth, MorphPredicate morphPredicate)
	{
		this.playerHealth = playerHealth;
		this.morphPredicate = morphPredicate;
	}
	
	@Override
	public boolean test(LootContext t)
	{
		Player player = null;
		
		if(t.hasParam(LootContextParams.THIS_ENTITY))
		{
			if(t.getParam(LootContextParams.THIS_ENTITY) instanceof Player castedPlayer)
				player = castedPlayer;
			else
				return false;
		}
				
		if(playerHealth != null && !playerHealth.matches(player.getHealth()))
			return false;
		
		if(morphPredicate != null)
		{
			IMorphCapability cap = MorphUtil.getCapOrNull(player);
			
			if(cap == null)
			{
				return false;
			}
			else
			{
				MorphItem morphItem = cap.getCurrentMorph().orElse(null);
				
				if(morphItem == null || !morphPredicate.matches(morphItem, cap))
				{
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	public LootItemConditionType getType()
	{
		return new LootItemConditionType(new Serializer());
	}
	
	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<PlayerAttributesPredicate>
	{
		@Override
		public void serialize(JsonObject pJson, PlayerAttributesPredicate pValue, JsonSerializationContext pSerializationContext)
		{
			if(pValue.playerHealth != null)
				pJson.add("player_health", pValue.playerHealth.serializeToJson());
			if(pValue.morphPredicate != null)
				pJson.add("current_morph", pValue.morphPredicate.serializeToJson());
		}

		@Override
		public PlayerAttributesPredicate deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext)
		{
			MinMaxBounds.Doubles playerHealth = null;
			MorphPredicate morphPredicate = null;
			
			if(pJson.has("player_health"))
				playerHealth = MinMaxBounds.Doubles.fromJson(pJson.get("player_health"));
			if(pJson.has("current_morph"))
				morphPredicate = MorphPredicate.fromJson(pJson.get("current_morph"));
			
			return new PlayerAttributesPredicate(playerHealth, morphPredicate);
		}
	}
}
