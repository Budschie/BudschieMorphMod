package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.morph.functionality.configurable.BlockPassthroughAbility;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BlockPassthroughAbilityAdapter implements IBlockPassthroughAbilityAdapter
{
	private BlockPassthroughAbility ability;
	
	@Override
	public void setAbilty(BlockPassthroughAbility ability)
	{
		this.ability = ability;
	}
	
	@SubscribeEvent
	public void onFovModification(FOVModifierEvent event)
	{
		if (this.ability.isInWeb(event.getEntity()))
		{
			if(ability.getSpeedMultiplier() != 0)
				event.setNewfov((event.getNewfov() / (ability.getSpeedMultiplier())));
		}
	}

	@Override
	public void register()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void unregister()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}
}
