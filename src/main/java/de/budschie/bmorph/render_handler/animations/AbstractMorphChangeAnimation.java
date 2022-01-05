package de.budschie.bmorph.render_handler.animations;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;

import de.budschie.bmorph.render_handler.IEntitySynchronizer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractMorphChangeAnimation
{	
	private Player player;
	
	private Entity transitionFrom;
	private ArrayList<IEntitySynchronizer> synchronizersFrom;
	
	private Entity transitionTo;
	private ArrayList<IEntitySynchronizer> synchronizersTo;
	
	private int ticks;
	private int animationDuration;
	
	public AbstractMorphChangeAnimation(Player player, Entity transitionFrom, ArrayList<IEntitySynchronizer> synchronizersFrom, Entity transitionTo, ArrayList<IEntitySynchronizer> synchronizersTo, int animationDuration)
	{
		this.player = player;
		
		this.transitionFrom = transitionFrom;
		this.transitionTo = transitionTo;
		
		this.synchronizersFrom = synchronizersFrom;
		this.synchronizersTo = synchronizersTo;
		
		this.animationDuration = animationDuration;
	}
	
	public int getTicks()
	{
		return ticks;
	}
	
	public Player getPlayer()
	{
		return player;
	}
	
	public int getAnimationDuration()
	{
		return animationDuration;
	}
	
	public Entity getTransitionFrom()
	{
		return transitionFrom;
	}
	
	public Entity getTransitionTo()
	{
		return transitionTo;
	}
	
	public ArrayList<IEntitySynchronizer> getSynchronizersFrom()
	{
		return synchronizersFrom;
	}
	
	public ArrayList<IEntitySynchronizer> getSynchronizersTo()
	{
		return synchronizersTo;
	}
	
	public void tick()
	{
		this.ticks++;
		
		synchronizersFrom.forEach(oldSync -> oldSync.applyToMorphEntity(transitionFrom, player));
		synchronizersTo.forEach(newSync -> newSync.applyToMorphEntity(transitionTo, player));
	}
	
	public abstract void render(PoseStack matrixStack, float partialRenderTicks, MultiBufferSource buffers, int light);
	
	public float getProgress()
	{
		return ((float)ticks) / ((float)animationDuration);
	}
	
	public float getProgress(float partialTicks)
	{
		return ((ticks) + partialTicks) / (animationDuration);
	}
}
