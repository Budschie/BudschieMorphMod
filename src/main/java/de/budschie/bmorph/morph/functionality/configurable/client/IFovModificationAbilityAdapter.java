package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.morph.functionality.configurable.FovModificationAbility;

public interface IFovModificationAbilityAdapter
{
	void setAbility(FovModificationAbility ability);
	void register();
	void unregister();
}
