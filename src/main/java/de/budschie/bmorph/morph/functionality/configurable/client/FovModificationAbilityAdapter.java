package de.budschie.bmorph.morph.functionality.configurable.client;

import de.budschie.bmorph.morph.functionality.configurable.FovModificationAbility;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FovModificationAbilityAdapter implements IFovModificationAbilityAdapter
{
	private FovModificationAbility ability;

	@Override
	public void setAbility(FovModificationAbility ability)
	{
		this.ability = ability;
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
	
	@SubscribeEvent
	public void onFovChanged(FOVModifierEvent event)
	{
		// Player is tracked, thus we shall modify their fov
		if(this.ability.isTracked(Minecraft.getInstance().player))
		{
			event.setNewfov(event.getNewfov() * this.ability.getFovMultiplier() + this.ability.getFovAdd());
		}
	}
}
