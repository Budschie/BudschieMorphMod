package de.budschie.bmorph.morph.functionality;

import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer;
import de.budschie.bmorph.network.ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket;
import de.budschie.bmorph.util.DynamicRegistry;

public class DynamicAbilityRegistry extends DynamicRegistry<Ability, ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket>
{
	@Override
	public ConfiguredAbilityPacket getPacket()
	{
		return new ConfiguredAbilitySynchronizer.ConfiguredAbilityPacket(entries.values());
	}
	
	@Override
	public void onRegister(Ability registeredObject)
	{
		super.onRegister(registeredObject);
		registeredObject.onRegister();
	}
	
	@Override
	public void onUnregister(Ability unregisteredObject)
	{
		super.onUnregister(unregisteredObject);
		unregisteredObject.onUnregister();
	}
}
