package de.budschie.bmorph.morph.functionality;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import de.budschie.bmorph.main.ServerSetup;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WebSpeedAbility extends AbstractEventAbility
{
	private static final UUID WEB_MOVEMENT_UUID = new UUID(1285, 7881);
	
	public WebSpeedAbility()
	{
		super();
	}
	
	@SubscribeEvent
	public void onBeingInWeb(ServerTickEvent event)
	{
		trackedPlayers.forEach(uuid ->
		{
			PlayerEntity player = ServerSetup.server.getPlayerList().getPlayerByUUID(uuid);
			
			if(player != null)
			{
				boolean containsCobweb = false;
				
			    AxisAlignedBB aabb = player.getBoundingBox();
			    BlockPos from = new BlockPos(aabb.minX + 0.001D, aabb.minY + 0.001D, aabb.minZ + 0.001D);
			    BlockPos to = new BlockPos(aabb.maxX - 0.001D, aabb.maxY - 0.001D, aabb.maxZ - 0.001D);
			    
			    loop:
			    for(int x = from.getX(); x <= to.getX(); x++)
			    {
			    	for(int y = from.getY(); y <= to.getY(); y++)
			    	{
				    	for(int z = from.getZ(); z <= to.getZ(); z++)
				    	{
				    		BlockPos pos = new BlockPos(x, y, z);
				    		ChunkPos chunk = new ChunkPos(pos);
				    		
				    		if(player.world.chunkExists(chunk.x, chunk.z) && player.world.getBlockState(pos).getBlock() == Blocks.COBWEB)
				    		{
				    			containsCobweb = true;
				    			break loop;
				    		}
				    	}
			    	}
			    }
			    
				if(containsCobweb && player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(WEB_MOVEMENT_UUID) == null)
					player.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(new AttributeModifier(WEB_MOVEMENT_UUID, "mvmntWeb", 7f, Operation.MULTIPLY_BASE));
				else if(!containsCobweb && player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(WEB_MOVEMENT_UUID) != null)
					player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WEB_MOVEMENT_UUID);
			}
		});
	}

	@Override
	public void disableAbility(PlayerEntity player, MorphItem disabledItem)
	{
		super.disableAbility(player, disabledItem);
		
		if(player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(WEB_MOVEMENT_UUID) != null)
			player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(WEB_MOVEMENT_UUID);
	}

	@Override
	public void onUsedAbility(PlayerEntity player, MorphItem currentMorph)
	{
		
	}
}
