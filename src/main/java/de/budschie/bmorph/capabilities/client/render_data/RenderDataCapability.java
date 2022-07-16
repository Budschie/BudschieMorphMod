package de.budschie.bmorph.capabilities.client.render_data;

import java.util.ArrayList;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.proxy_entity_cap.ProxyEntityCapabilityInstance;
import de.budschie.bmorph.morph.MorphUtil;
import de.budschie.bmorph.render_handler.EntitySynchronizerRegistry;
import de.budschie.bmorph.render_handler.IEntitySynchronizer;
import de.budschie.bmorph.render_handler.IEntitySynchronizerWithRotation;
import de.budschie.bmorph.render_handler.InitializeMorphEntityEvent;
import de.budschie.bmorph.render_handler.animations.AbstractMorphChangeAnimation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class RenderDataCapability implements IRenderDataCapability
{
	private Optional<AbstractMorphChangeAnimation> animation = Optional.empty();
	
	private Entity cachedEntity;
	private ArrayList<IEntitySynchronizer> cachedSynchronizers;
	private ArrayList<IEntitySynchronizerWithRotation> cachedRotationSynchronizers;
	
	@Override
	public void setAnimation(Optional<AbstractMorphChangeAnimation> animation)
	{
		this.animation = animation;
	}

	@Override
	public void tickAnimation()
	{
		if(animation.isPresent())
		{
			animation.get().tick();
			
			if(animation.get().getAnimationDuration() == animation.get().getTicks())
				animation = Optional.empty();
		}
	}

	@Override
	public void renderAnimation(Player player, PoseStack poseStack, float partialTicks, MultiBufferSource buffer, int light)
	{
		animation.ifPresent(presentAnimation -> presentAnimation.render(poseStack, partialTicks, buffer, light));
	}

	@Override
	public boolean hasAnimation()
	{
		return animation.isPresent();
	}

	@Override
	public Entity getOrCreateCachedEntity(Player player)
	{		
		if(cachedEntity == null)
		{
			// We put this statement below here instead of directly putting it into the
			// direct block belonging to the method because we want to reduce the amount
			// this capability is being accessed.
			IMorphCapability cap = MorphUtil.getCapOrNull(player);
			
			if(cap != null && cap.getCurrentMorph().isPresent())
			{
				cachedEntity = cap.getCurrentMorph().get().createEntity(player.level);
				cachedEntity.getCapability(ProxyEntityCapabilityInstance.PROXY_ENTITY_CAP).ifPresent(proxyEntityCap -> proxyEntityCap.setProxyEntity(true));
				MinecraftForge.EVENT_BUS.post(new InitializeMorphEntityEvent(player, cachedEntity));
			}
		}
		
		return cachedEntity;
	}

	@Override
	public ArrayList<IEntitySynchronizer> getOrCreateCachedSynchronizers(Player player)
	{
		if(cachedSynchronizers == null)
		{
			Entity entity = getOrCreateCachedEntity(player);
			
			// We need this check in case getOrCreateCachedEntity returns null because we are not morphed.
			if(entity != null)
			{
				cachedSynchronizers = EntitySynchronizerRegistry.getSynchronizersForEntity(entity);
			}
		}
		
		return cachedSynchronizers;
	}
	
	@Override
	public ArrayList<IEntitySynchronizerWithRotation> getOrCreateCachedRotationSynchronizers(Player player)
	{		
		if(cachedRotationSynchronizers == null)
		{
			getOrCreateCachedSynchronizers(player);
			
			this.cachedRotationSynchronizers = new ArrayList<>();
			
			for(IEntitySynchronizer sync : this.cachedSynchronizers)
			{
				if(sync instanceof IEntitySynchronizerWithRotation withRotation)
					this.cachedRotationSynchronizers.add(withRotation);
			}
		}
		
		return cachedRotationSynchronizers;
	}

	@Override
	public void setEntity(Entity entity)
	{
		invalidateCache();
		
		this.cachedEntity = entity;
	}

	@Override
	public void invalidateCache()
	{
		if(this.cachedEntity != null)
			this.cachedEntity.remove(RemovalReason.DISCARDED);
		
		this.cachedEntity = null;
		this.cachedSynchronizers = null;
		this.cachedRotationSynchronizers = null;
	}
}
