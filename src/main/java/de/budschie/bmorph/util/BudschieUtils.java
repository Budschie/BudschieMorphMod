package de.budschie.bmorph.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.LazyRegistryWrapper;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

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
	
	public static LootItemCondition[][] resolveConditions(List<List<LazyRegistryWrapper<LootItemCondition>>> unresolved)
	{
		LootItemCondition[][] lootItemConditions = new LootItemCondition[unresolved.size()][];

		for (int i = 0; i < unresolved.size(); i++)
		{
			List<LazyRegistryWrapper<LootItemCondition>> innerList = unresolved.get(i);
			lootItemConditions[i] = new LootItemCondition[innerList.size()];

			for (int j = 0; j < innerList.size(); j++)
			{
				LootItemCondition resolved = innerList.get(j).getWrappedType();

				if (resolved == null)
					return null;

				lootItemConditions[i][j] = resolved;
			}
		}
		
		return lootItemConditions;
	}
	
	public static boolean testPredicates(LootItemCondition[][] conditions, Supplier<LootContext> context)
	{
		if(conditions == null)
			return false;
		
		boolean predicateTrue = true;
		
		iterateOverList:
		for(LootItemCondition[] innerList : conditions)
		{
			for(LootItemCondition innerIteration : innerList)
			{
				// If we meet at least one predicate that is true, we can skip evaluating the others and go straight to the next set of predicates
				if(innerIteration.test(context.get()))
						continue iterateOverList;
			}
			
			// If we did not go to the next list of predicates, this means that there was no predicate true. This means that AND will return false => we set "predicateTrue" to false and break out of the loop.
			predicateTrue = false;
			break iterateOverList;
		}
		
		return predicateTrue;
	}
	
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
