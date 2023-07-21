package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.morph.functionality.configurable.BlockPassthroughAbility;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ViewportEvent.ComputeFov;
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
	public void onFovModification(ComputeFov event)
	{
		if (event.getCamera().getEntity() instanceof Player player && this.ability.isInWeb(player))
		{
			if(ability.getSpeedMultiplier() != 0)
				event.setFOV((event.getFOV() / (ability.getSpeedMultiplier())));
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
