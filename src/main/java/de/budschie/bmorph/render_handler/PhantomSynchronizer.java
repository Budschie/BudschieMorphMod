package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityInstance;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatus;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;

public class PhantomSynchronizer implements IEntitySynchronizerWithRotation
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Phantom;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player)
	{
	}

	@Override
	public void updateMorphRotation(Entity morphEntity, Player player)
	{
		Phantom phantom = (Phantom) morphEntity;
		
		player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
		{
			float correctedXRot = -phantom.getXRot();

			morphEntity.xRotO = morphEntity.getXRot();
			
			if (cap.getGlideStatus() == GlideStatus.CHARGE)
			{
				// Just look up/down
				// morphEntity.xRotO = cap.getChargeDirection().getRotX();
				morphEntity.setXRot(cap.getChargeDirection().getRotX());
				// morphEntity.xRotO = cap.getChargeDirection().getRotX();
			}
			else if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
			{
				// Ease in or out
				boolean invert = cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT;
				
				// Partial ticks removed. I hope this won't turn out to be a bad thing.
				float progress = BudschieUtils.getPhantomEaseFunction(cap.getTransitionTime(), cap.getMaxTransitionTime());
				float oldProgress = BudschieUtils.getPhantomEaseFunction(Math.max(cap.getTransitionTime() - 1, 0), cap.getMaxTransitionTime());
				
				if(invert)
				{
					progress = 1 - progress;
					oldProgress = 1 - oldProgress;
				}
				
				progress = Mth.lerp(Minecraft.getInstance().getDeltaFrameTime(), oldProgress, progress);
								
				// We can use the rot and old value to interpolate instead of using the partial ticks to prevent stuttery movement
				// morphEntity.xRotO = morphEntity.getXRot();
				morphEntity.setXRot(Mth.lerp(progress, cap.getChargeDirection().getRotX(), correctedXRot));
				// morphEntity.xRotO = Mth.lerp(oldProgress, cap.getChargeDirection().getRotX(), correctedXRot);
			}
			else
			{
				// phantom.xRotO = correctedXRotO;
				phantom.setXRot(correctedXRot);
			}
		});
	}	
}
