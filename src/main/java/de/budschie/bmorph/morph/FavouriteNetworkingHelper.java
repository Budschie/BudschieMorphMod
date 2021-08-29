package de.budschie.bmorph.morph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphRequestFavouriteChange.MorphRequestFavouriteChangePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.util.LazyOptional;

public class FavouriteNetworkingHelper
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	// DRY CODE
	private static void internalAddFavouriteMorph(boolean add, int indexInMorphArray)
	{
		PlayerEntity player = Minecraft.getInstance().player;
		
		LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			
			if(add)
				resolved.getFavouriteList().addFavourite(indexInMorphArray);
			else
				resolved.getFavouriteList().removeFavourite(indexInMorphArray);
			
			MorphRequestFavouriteChangePacket favouritePacket = new MorphRequestFavouriteChangePacket(add, indexInMorphArray);
			MainNetworkChannel.INSTANCE.sendToServer(favouritePacket);
		}
		else
		{
			LOGGER.warn("Can't " + (add ? "add" : "remove") + "morph " + indexInMorphArray + " as a favourite, as the capability for morphs is not loaded yet.");
		}
	}	
	public static void addFavouriteMorph(int indexInMorphArray)
	{
		internalAddFavouriteMorph(true, indexInMorphArray);
	}
	
	public static void removeFavouriteMorph(int indexInMorphArray)
	{
		internalAddFavouriteMorph(false, indexInMorphArray);
	}
}
