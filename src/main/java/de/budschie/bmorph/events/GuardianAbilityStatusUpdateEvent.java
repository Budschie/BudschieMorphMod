package de.budschie.bmorph.events;

import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;

public class GuardianAbilityStatusUpdateEvent extends Event
{
	private Player player;
	private IGuardianBeamCapability capability;
	private boolean invalidated;
	
	public GuardianAbilityStatusUpdateEvent(Player player, IGuardianBeamCapability capability, boolean invalidated)
	{
		this.player = player;
		this.capability = capability;
		this.invalidated = invalidated;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public IGuardianBeamCapability getCapability()
	{
		return capability;
	}
	
	public boolean isInvalidated()
	{
		return invalidated;
	}
}
