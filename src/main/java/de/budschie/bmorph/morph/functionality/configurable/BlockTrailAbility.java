package de.budschie.bmorph.morph.functionality.configurable;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.functionality.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class BlockTrailAbility extends Ability
{
	public static final Codec<BlockTrailAbility> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockStateProvider.CODEC.fieldOf("trail_block").forGetter(BlockTrailAbility::getTrailBlock),
			Codec.BOOL.optionalFieldOf("should_only_spawn_in_cold_biome", false).forGetter(BlockTrailAbility::shouldSpawnOnlyInColdBiome))
			.apply(instance, BlockTrailAbility::new));
	
	private BlockStateProvider trailBlock;
	private boolean spawnOnlyInColdBiome;
	
	public BlockTrailAbility(BlockStateProvider trailBlock, boolean spawnOnlyInColdBiome)
	{
		this.trailBlock = trailBlock;
		this.spawnOnlyInColdBiome = spawnOnlyInColdBiome;
	}
	
	public BlockStateProvider getTrailBlock()
	{
		return trailBlock;
	}
	
	public boolean shouldSpawnOnlyInColdBiome()
	{
		return spawnOnlyInColdBiome;
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			for(UUID playerId : trackedPlayers)
				placeTrailBlocks(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerId));
		}
	}
	
	private void placeTrailBlocks(Player player)
	{		
		// Unwinded sheesh
		for (int placePos = 0; placePos < 4; ++placePos)
		{
			int x = Mth.floor(player.getX() + ((placePos % 2 == 0) ? -0.25f : 0.25f));
			int y = Mth.floor(player.getY());
			int z = Mth.floor(player.getZ() + ((placePos < 2) ? -0.25f : 0.25f));
			
			BlockPos blockPos = new BlockPos(x, y, z);
			
			BlockState blockState = trailBlock.getState(player.getRandom(), blockPos);
			
			if (player.level.isEmptyBlock(blockPos) && (player.level.getBiome(blockPos).value().shouldSnowGolemBurn(blockPos) || !shouldSpawnOnlyInColdBiome()) && blockState.canSurvive(player.level, blockPos))
			{
				player.level.setBlockAndUpdate(blockPos, trailBlock.getState(player.getRandom(), blockPos));
			}
		}
	}
	
	@Override
	public boolean isAbleToReceiveEvents()
	{
		return true;
	}
}
