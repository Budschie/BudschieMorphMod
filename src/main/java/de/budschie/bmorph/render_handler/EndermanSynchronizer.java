package de.budschie.bmorph.render_handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EndermanSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof EnderMan;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
		EnderMan enderMan = (EnderMan) morphEntity;
		
		BlockState block = null;
		
		if(player.getMainHandItem() != null && player.getMainHandItem().getItem() instanceof BlockItem blockItem)
		{
			Block carriedBlock = blockItem.getBlock();
			
			if(carriedBlock != null)
				block = carriedBlock.defaultBlockState();
		}
		
		enderMan.setCarriedBlock(block);
	}
}
