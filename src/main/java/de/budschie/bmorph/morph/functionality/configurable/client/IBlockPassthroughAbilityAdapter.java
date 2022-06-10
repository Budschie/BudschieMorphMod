package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.morph.functionality.configurable.BlockPassthroughAbility;

public interface IBlockPassthroughAbilityAdapter
{
	void setAbilty(BlockPassthroughAbility ability);
	
	void register();
	void unregister();
}
