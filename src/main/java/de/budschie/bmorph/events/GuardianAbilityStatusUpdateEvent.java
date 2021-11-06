package de.budschie.bmorph.events;

import de.budschie.bmorph.capabilities.guardian.IGuardianBeamCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.Event;

public class GuardianAbilityStatusUpdateEvent extends Event
{
	private PlayerEntity player;
	private IGuardianBeamCapability capability;
	
	public GuardianAbilityStatusUpdateEvent(PlayerEntity player, IGuardianBeamCapability capability)
	{
		this.player = player;
		this.capability = capability;
	}
	
	public PlayerEntity getPlayer()
	{
		return player;
	}
	
	public IGuardianBeamCapability getCapability()
	{
		return capability;
	}
}
