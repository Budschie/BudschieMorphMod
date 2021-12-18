package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;

import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class DynamicAbilityRegistry
{
	private HashMap<ResourceLocation, Ability> abilities = new HashMap<>();
	
	public void registerAbility(Ability ability)
	{
		if(abilities.containsKey(ability.getResourceLocation()))
			throw new IllegalArgumentException(String.format("The key %s already exists.", ability.getResourceLocation()));
		else
		{
			System.out.println("Registered " + ability.getResourceLocation());
			abilities.put(ability.getResourceLocation(), ability);
			ability.onRegister();
		}
	}
	
	public boolean isEmpty()
	{
		return abilities.isEmpty();
	}
	
	public Ability getAbility(ResourceLocation key)
	{
		return abilities.get(key);
	}
	
	public boolean doesAbilityExist(ResourceLocation key)
	{
		return abilities.containsKey(key);
	}
	
	public void unregisterAll()
	{
		abilities.forEach((name, ability) -> ability.onUnregister());
		abilities.clear();
	}
	
	public void syncWithClients()
	{
		if(!abilities.isEmpty() && !BudschieUtils.isLocalWorld())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket(abilities.values()));
		else
			System.out.println("Skipping registry sync as world is local.");
	}
	
	public void syncWithClient(ServerPlayer player)
	{
		if(!abilities.isEmpty() && !BudschieUtils.isLocalWorld())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket(abilities.values()));
		else
			System.out.println("Skipping registry sync as world is local.");
	}	
}
