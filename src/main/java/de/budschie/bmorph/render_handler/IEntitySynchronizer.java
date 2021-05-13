package de.budschie.bmorph.render_handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public interface IEntitySynchronizer
{
	/** This method should return true if attributes from the given entity shall be set. **/
	boolean appliesToMorph(Entity morphEntity);
	
	/** This method will be called when {@link IEntitySynchronizer#appliesToMorph(Entity)} returns {@code true}. 
	 * <br><br> It should read attributes from the player and set them properly in the entity, e. g. if you are morphed into a skeleton, and the player holds a bow, this very bow should then be transferred into the skeletons inventory.
	 **/
	void applyToMorphEntity(Entity morphEntity, PlayerEntity player);
}
