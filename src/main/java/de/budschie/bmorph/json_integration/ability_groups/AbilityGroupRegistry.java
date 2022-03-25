package de.budschie.bmorph.json_integration.ability_groups;

import java.util.Collection;
import java.util.HashSet;

import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry.AbilityGroup;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.network.AbilityGroupSync.AbilityGroupSyncPacket;
import de.budschie.bmorph.util.DynamicRegistry;
import de.budschie.bmorph.util.IDynamicRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AbilityGroupRegistry extends DynamicRegistry<AbilityGroup, AbilityGroupSyncPacket>
{
	public Runnable fillRegistry;

	@Override
	public AbilityGroupSyncPacket getPacket()
	{
		return AbilityGroupSyncPacket.serverPacket(values());
	}
	
	@Override
	public AbilityGroup getEntry(ResourceLocation key)
	{
		fillRegistryIfPossible();
		return super.getEntry(key);
	}
	
	@Override
	public boolean hasEntry(ResourceLocation key)
	{
		fillRegistryIfPossible();
		return super.hasEntry(key);
	}
	
	@Override
	public Collection<AbilityGroup> values()
	{
		fillRegistryIfPossible();
		return super.values();
	}
	
	@Override
	public boolean isEmpty()
	{
		fillRegistryIfPossible();
		return super.isEmpty();
	}
	
	@Override
	public void syncWithClient(ServerPlayer player)
	{
		fillRegistryIfPossible();
		super.syncWithClient(player);
	}
	
	@Override
	public void syncWithClients()
	{
		fillRegistryIfPossible();
		super.syncWithClients();
	}
	
	public void setFillRegistry(Runnable fillRegistry)
	{
		this.fillRegistry = fillRegistry;
	}
	
	private void fillRegistryIfPossible()
	{
		if(fillRegistry != null)
		{
			unregisterAll();
			fillRegistry.run();
			fillRegistry = null;
		}
	}
	
	public static class AbilityGroup implements IDynamicRegistryObject
	{
		private HashSet<Ability> abilities = new HashSet<>();
		private ResourceLocation resourceLocation;
		
		public AbilityGroup(ResourceLocation resourceLocation)
		{
			this.resourceLocation = resourceLocation;
		}
		
		public void addAbility(Ability ability)
		{
			this.abilities.add(ability);
		}
		
		public Collection<Ability> getAbilities()
		{
			return abilities;
		}
		
		public boolean containsAbility(Ability ability)
		{
			return abilities.contains(ability);
		}

		@Override
		public ResourceLocation getResourceLocation()
		{
			return resourceLocation;
		}

		@Override
		public void setResourceLocation(ResourceLocation name)
		{
			this.resourceLocation = name;
		}
	}	
}
