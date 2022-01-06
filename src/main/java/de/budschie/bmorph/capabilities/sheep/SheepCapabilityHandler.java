package de.budschie.bmorph.capabilities.sheep;

import de.budschie.bmorph.capabilities.common.CommonCapabilityHandler;
import de.budschie.bmorph.network.MorphSheepSheared.MorphSheepShearedPacket;
import net.minecraft.world.entity.player.Player;

public class SheepCapabilityHandler extends CommonCapabilityHandler<ISheepCapability, MorphSheepShearedPacket>
{
	public static final SheepCapabilityHandler INSTANCE = new SheepCapabilityHandler();
	
	public SheepCapabilityHandler()
	{
		super(SheepCapabilityInstance.SHEEP_CAP);
	}

	@Override
	protected MorphSheepShearedPacket createPacket(Player player, ISheepCapability capability)
	{
		return new MorphSheepShearedPacket(capability.isSheared());
	}
	
	public void setSheared(Player player, boolean value)
	{
		ISheepCapability cap = player.getCapability(SheepCapabilityInstance.SHEEP_CAP).resolve().orElse(null);
		
		if(cap != null)
		{
			// Skip syncing if the value is the same
			if(cap.isSheared() == value)
				return;
			
			cap.setSheared(value);
			
			synchronizeWithClients(player);
		}
	}
	
	public boolean isSheared(Player player)
	{
		ISheepCapability cap = player.getCapability(SheepCapabilityInstance.SHEEP_CAP).resolve().orElse(null);
		
		if(cap != null)
		{
			return cap.isSheared();
		}
		
		throw new IllegalStateException("This method may not be called when the sheep capability isn't setup yet. If you see this error, please contact the mod author via their GitHub repository of this project.");
	}
}
