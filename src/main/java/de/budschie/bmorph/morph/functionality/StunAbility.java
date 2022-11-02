package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import de.budschie.bmorph.capabilities.AbilitySerializationContext;
import de.budschie.bmorph.capabilities.AbilitySerializationContext.AbilitySerializationObject;
import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public abstract class StunAbility extends Ability
{
	private int stun;
	private HashMap<UUID, Integer> delayHashMap = new HashMap<>();
	
	public StunAbility(int stun)
	{
		this.stun = stun;
	}
	
	public int getStun()
	{
		return stun;
	}
	
	public int getStunTimeLeftFor(Player player)
	{
		return delayHashMap.containsKey(player.getUUID()) ? delayHashMap.get(player.getUUID()) - BudschieUtils.getUniversalTickTime() : 0;
	}
	
	public boolean isCurrentlyStunned(UUID player)
	{
		return delayHashMap.getOrDefault(player, 0) > BudschieUtils.getUniversalTickTime();
	}
	
	@Override
	public void removePlayerReferences(Player playerRefToRemove)
	{
		super.removePlayerReferences(playerRefToRemove);
		
		delayHashMap.remove(playerRefToRemove.getUUID());
	}
	
	public void stun(UUID player)
	{
		delayHashMap.put(player, BudschieUtils.getUniversalTickTime() + stun);
	}
	
	public void stun(UUID player, int nonDefaultStunTime)
	{
		delayHashMap.put(player, BudschieUtils.getUniversalTickTime() + nonDefaultStunTime);
	}
	
	@Override
	public void serialize(Player player, AbilitySerializationContext context, boolean canSaveTransientData)
	{
		super.serialize(player, context, canSaveTransientData);
		
		// Save time left instead of timestamp because we can then recalculate that whole stuff
		context.getOrCreateSerializationObjectForAbility(this).getOrCreatePersistentTag().putInt("stun_stunned_for", getStunTimeLeftFor(player));
	}
	
	@Override
	public void deserialize(Player player, AbilitySerializationContext context)
	{
		super.deserialize(player, context);
		
		AbilitySerializationObject object = context.getSerializationObjectForAbilityOrNull(this);
		
		if(object != null)
		{
			if(object.getPersistentTag().isPresent())
			{
				CompoundTag persistentTag = object.getPersistentTag().get();
				
				stun(player.getUUID(), persistentTag.getInt("stun_stunned_for"));
			}
		}
	}
}
