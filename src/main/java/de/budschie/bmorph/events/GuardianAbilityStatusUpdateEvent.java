package de.budschie.bmorph.events;

import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class GuardianAbilityStatusUpdateEvent extends Event
{
	private Player player;
	private IGuardianBeamCapability capability;
	
	public GuardianAbilityStatusUpdateEvent(Player player, IGuardianBeamCapability capability)
	{
		this.player = player;
		this.capability = capability;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public IGuardianBeamCapability getCapability()
	{
		return capability;
	}
}
