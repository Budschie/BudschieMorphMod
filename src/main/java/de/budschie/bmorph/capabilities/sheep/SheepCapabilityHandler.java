package de.budschie.bmorph.capabilities.sheep;

import de.budschie.bmorph.capabilities.common.CommonCapabilityHandler;
import de.budschie.bmorph.network.MorphSheepSheared.MorphSheepShearedPacket;
import net.minecraft.world.entity.player.Player;

public class SheepCapabilityHandler extends CommonCapabilityHandler<ISheepCapability, MorphSheepShearedPacket>
{
	public SheepCapabilityHandler()
	{
		super(SheepCapabilityInstance.SHEEP_CAP);
	}

	@Override
	protected MorphSheepShearedPacket createPacket(Player player, ISheepCapability capability)
	{
		return new MorphSheepShearedPacket(capability.isSheared());
	}
}
