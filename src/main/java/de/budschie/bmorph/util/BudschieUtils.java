package de.budschie.bmorph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.budschie.bmorph.main.ServerSetup;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;

public class BudschieUtils
{
//	public static boolean isLocalWorld()
//	{
//		boolean isLan;
//		
//		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
//		{
//			if(Minecraft.getInstance().isLocalServer())
//			{
//				isLan = true;
//			}
//		});
//		
//		return FMLEnvironment.dist == Dist.CLIENT && ServerSetup.server != null;
//	}
	
//	public static boolean willSyncWith(Player player)
//	{
//		
//	}
	
	public static float getPhantomEaseFunction(float currentTime, float maxTime)
	{
		return (float) Math.pow((currentTime) / (maxTime), 1);
	}
	
	/** Convert a timestamp to a relative time that is left until this timestamp is reached. **/
	public static int convertToRelativeTime(int timestampUntilFinished)
	{
		return timestampUntilFinished - ServerSetup.server.getTickCount();
	}
	
	/** Convert a relative time to a timestamp. **/
	public static int convertToAbsoluteTime(int relativeTime)
	{
		return relativeTime + ServerSetup.server.getTickCount();
	}
	
	public static List<ServerPlayer> getPlayersTrackingEntity(ServerPlayer player)
	{
		ServerChunkCache chunkCache = (ServerChunkCache) player.level.getChunkSource();
		
		List<ServerPlayer> serverPlayers = new ArrayList<>();
		Set<ServerPlayerConnection> trackedPlayers = chunkCache.chunkMap.entityMap.get(player.getId()).seenBy;
		
		for(ServerPlayerConnection connection : trackedPlayers)
			serverPlayers.add(connection.getPlayer());
		
		return serverPlayers;
	}
	
	public static List<ServerPlayer> getPlayersTrackingEntityAndSelf(ServerPlayer player)
	{
		List<ServerPlayer> list = getPlayersTrackingEntity(player);
		list.add(player);
		
		return list;
	}
}
