package de.budschie.bmorph.capabilities.parrot_dance;

import de.budschie.bmorph.capabilities.common.CommonCapabilityHandler;
import de.budschie.bmorph.network.ParrotDanceSync.ParrotDanceSyncPacket;
import net.minecraft.world.entity.player.Player;

public class ParrotDanceCapabilityHandler extends CommonCapabilityHandler<IParrotDanceCapability, ParrotDanceSyncPacket>
{
	public static final ParrotDanceCapabilityHandler INSTANCE = new ParrotDanceCapabilityHandler();
	
	public ParrotDanceCapabilityHandler()
	{
		super(ParrotDanceCapabilityInstance.PARROT_CAP);
	}
	
	public void setDancingServer(Player player, boolean dancing)
	{
		player.getCapability(ParrotDanceCapabilityInstance.PARROT_CAP).ifPresent(cap ->
		{
			cap.setDancing(dancing);
			
			synchronizeWithClients(player);
		});
	}
	
	@Override
	protected ParrotDanceSyncPacket createPacket(Player player, IParrotDanceCapability capability)
	{
		return new ParrotDanceSyncPacket(capability.isDancing());
	}
}
