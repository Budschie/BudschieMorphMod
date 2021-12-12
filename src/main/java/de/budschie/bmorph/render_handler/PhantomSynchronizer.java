package de.budschie.bmorph.render_handler;

import de.budschie.bmorph.capabilities.phantom_glide.GlideCapabilityInstance;
import de.budschie.bmorph.capabilities.phantom_glide.GlideStatus;
import de.budschie.bmorph.util.BudschieUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;

public class PhantomSynchronizer implements IEntitySynchronizer
{
	@Override
	public boolean appliesToMorph(Entity morphEntity)
	{
		return morphEntity instanceof Phantom;
	}

	@Override
	public void applyToMorphEntity(Entity morphEntity, Player player, float partialTicks)
	{
		Phantom phantom = (Phantom) morphEntity;
		
		player.getCapability(GlideCapabilityInstance.GLIDE_CAP).ifPresent(cap ->
		{
			float correctedXRot = -phantom.getXRot();
			float correctedXRotO = -phantom.xRotO;

			if (cap.getGlideStatus() == GlideStatus.CHARGE)
			{
				// Just look up/down
				morphEntity.xRotO = cap.getChargeDirection().getRotX();
				morphEntity.setXRot(cap.getChargeDirection().getRotX());
			}
			else if(cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_IN || cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT)
			{
				// Ease in or out
				boolean invert = cap.getGlideStatus() == GlideStatus.CHARGE_TRANSITION_OUT;
				
				float progress = BudschieUtils.getPhantomEaseFunction(cap.getTransitionTime() - partialTicks, cap.getMaxTransitionTime());
				
				if(invert)
					progress = 1 - progress;
				
				morphEntity.xRotO = Mth.lerp(progress, cap.getChargeDirection().getRotX(), correctedXRotO);
				morphEntity.setXRot(Mth.lerp(progress, cap.getChargeDirection().getRotX(), correctedXRot));
			}
			else
			{
				phantom.xRotO = correctedXRotO;
				phantom.setXRot(correctedXRot);
			}
		});
	}	
}
