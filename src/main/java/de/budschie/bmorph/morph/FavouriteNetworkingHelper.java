package de.budschie.bmorph.morph;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.network.MainNetworkChannel;
import de.budschie.bmorph.network.MorphRequestFavouriteChange.MorphRequestFavouriteChangePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class FavouriteNetworkingHelper
{
	private static final Logger LOGGER = LogManager.getLogger();
	
	// DRY CODE
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	private static void internalAddFavouriteMorph(boolean add, int indexInMorphArray)
	{
		LazyOptional<IMorphCapability> cap = Minecraft.getInstance().player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			internalAddFavouriteMorph(add, resolved.getMorphList().getMorphArrayList().get(indexInMorphArray).getUUID());
		}
	}
	
	private static void internalAddFavouriteMorph(boolean add, UUID morphItemKey)
	{
		Player player = Minecraft.getInstance().player;
		
		LazyOptional<IMorphCapability> cap = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
		
		if(cap.isPresent())
		{
			IMorphCapability resolved = cap.resolve().get();
			
			if(add)
				resolved.getFavouriteList().addFavourite(morphItemKey);
			else
				resolved.getFavouriteList().removeFavourite(morphItemKey);
			
			MorphRequestFavouriteChangePacket favouritePacket = new MorphRequestFavouriteChangePacket(add, morphItemKey);
			MainNetworkChannel.INSTANCE.sendToServer(favouritePacket);
		}
		else
		{
			LOGGER.warn("Can't " + (add ? "add" : "remove") + "morph " + morphItemKey + " as a favourite, as the capability for morphs is not loaded yet.");
		}
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public static void addFavouriteMorph(int indexInMorphArray)
	{
		internalAddFavouriteMorph(true, indexInMorphArray);
	}
	
	@Deprecated(since = "1.18.2-1.0.2", forRemoval = true)
	public static void removeFavouriteMorph(int indexInMorphArray)
	{
		internalAddFavouriteMorph(false, indexInMorphArray);
	}
	
	public static void addFavouriteMorph(UUID morphItemKey)
	{
		internalAddFavouriteMorph(true, morphItemKey);
	}
	
	public static void removeFavouriteMorph(UUID morphItemKey)
	{
		internalAddFavouriteMorph(false, morphItemKey);
	}
}
