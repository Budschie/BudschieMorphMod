package de.budschie.bmorph.morph.functionality;

import java.util.HashMap;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer;
import de.budschie.bmorph.network.MainNetworkChannel;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;

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
		if(!abilities.isEmpty() && !isLocalWorld())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.ALL.noArg(), new ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket(abilities.values()));
		else
			System.out.println("Skipping registry sync as world is local.");
	}
	
	public void syncWithClient(ServerPlayerEntity player)
	{
		if(!abilities.isEmpty() && !isLocalWorld())
			MainNetworkChannel.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket(abilities.values()));
		else
			System.out.println("Skipping registry sync as world is local.");
	}
	
	private boolean isLocalWorld()
	{
		return FMLEnvironment.dist == Dist.CLIENT && ServerSetup.server != null;
	}
}
