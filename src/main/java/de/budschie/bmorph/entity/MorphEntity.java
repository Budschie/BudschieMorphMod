package de.budschie.bmorph.entity;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.PlayerMorphItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;

public class MorphEntity extends Entity
{
	private static final PlayerMorphItem DEFAULT = MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, new GameProfile(new UUID(0, 0), "SteveMcSteve"));
	
	@SuppressWarnings("unchecked")
	private static final DataParameter<MorphItem> MORPH_ITEM = (DataParameter<MorphItem>) EntityDataManager.createKey(MorphEntity.class, EntityRegistry.MORPH_SERIALIZER.get().getSerializer());
	
	public MorphEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	public MorphEntity(World world, MorphItem morphItem)
	{
		super(EntityRegistry.MORPH_ENTITY.get(), world);
		this.getDataManager().set(MORPH_ITEM, morphItem);
	}
	
	@Override
	public void remove(boolean keepData)
	{
		super.remove(keepData);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(!this.world.isRemote)
		{
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, getBoundingBox().offset(getPosition()));
			
			for(Entity entity : list)
			{
				if(entity instanceof PlayerEntity)
				{
					PlayerEntity player = (PlayerEntity) entity;
					
					LazyOptional<IMorphCapability> lazyCaps = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
					
					if(!this.world.isRemote)
					{
						if(lazyCaps.isPresent())
						{
							IMorphCapability resolvedCaps = lazyCaps.resolve().get();
							
							if(!resolvedCaps.getMorphList().contains(getMorphItem()))
							{
								resolvedCaps.getMorphList().addToMorphList(getMorphItem());
								resolvedCaps.syncMorphAcquisition(player, getMorphItem());
								this.remove();
								
								this.world.playSound(null, getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.AMBIENT, 2, (this.rand.nextFloat() - 0.5f) + 1);
							}
						}
					}
				}
			}
		}		
	}
 
	@Override
	protected void registerData()
	{
		this.dataManager.register(MORPH_ITEM, DEFAULT);
	}

	@Override
	protected void readAdditional(CompoundNBT nbt)
	{
		this.dataManager.set(MORPH_ITEM, MorphHandler.deserializeMorphItem(nbt.getCompound("MorphData")));
	}

	@Override
	protected void writeAdditional(CompoundNBT nbt)
	{
		nbt.put("MorphData", this.dataManager.get(MORPH_ITEM).serialize());
	}
	
	public MorphItem getMorphItem()
	{
		return this.dataManager.get(MORPH_ITEM);
	}

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}	
	
	@Override
	public boolean isInvisible()
	{
		return false;
	}
}
