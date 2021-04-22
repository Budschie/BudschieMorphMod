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
		
//        this.prevPosX = this.getPosX();
//        this.prevPosY = this.getPosY();
//        this.prevPosZ = this.getPosZ();

		//this.move(MoverType.SELF, new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()));
		
//        this.prevPosX = this.getPosX();
//        this.prevPosY = this.getPosY();
//        this.prevPosZ = this.getPosZ();
//        float f = this.getEyeHeight() - 0.11111111F;
//        
//		this.prevPosX = this.getPosX();
//		this.prevPosY = this.getPosY();
//		this.prevPosZ = this.getPosZ();
//		Vector3d vector3d = this.getMotion();
//		float f = this.getEyeHeight() - 0.11111111F;
//		if (!this.hasNoGravity())
//		{
//			this.setMotion(this.getMotion().add(0.0D, -0.04D, 0.0D));
//		}
//
//		if (this.world.isRemote)
//		{
//			this.noClip = false;
//		} else
//		{
//			this.noClip = !this.world.hasNoCollisions(this);
//			if (this.noClip)
//			{
//				this.pushOutOfBlocks(this.getPosX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D,
//						this.getPosZ());
//			}
//		}
//
//		if (!this.onGround || horizontalMag(this.getMotion()) > (double) 1.0E-5F
//				|| (this.ticksExisted + this.getEntityId()) % 4 == 0)
//		{
//			//this.move(MoverType.SELF, this.getMotion());
//			float f1 = 0.98F;
//			if (this.onGround)
//			{
//				f1 = this.world.getBlockState(new BlockPos(this.getPosX(), this.getPosY() - 1.0D, this.getPosZ()))
//						.getSlipperiness(world, new BlockPos(this.getPosX(), this.getPosY() - 1.0D, this.getPosZ()),
//								this)
//						* 0.98F;
//			}
//
//			this.setMotion(this.getMotion().mul((double) f1, 0.98D, (double) f1));
//			if (this.onGround)
//			{
//				Vector3d vector3d1 = this.getMotion();
//				if (vector3d1.y < 0.0D)
//				{
//					this.setMotion(vector3d1.mul(1.0D, -0.5D, 1.0D));
//				}
//			}
//		}
//		
//		this.move(MoverType.SELF, new Vector3d(0, .01f, 0));
		
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
		
		//this.setPosition(getPosX() + this.getMotion().x, getPosY() + this.getMotion().y, getPosZ() + this.getMotion().z);
		
		//ticksExisted++;
	}
	
//	@Override
//	public void onCollideWithPlayer(PlayerEntity entityIn)
//	{
//		LazyOptional<IMorphCapability> lazyCaps = entityIn.getCapability(MorphCapabilityAttacher.MORPH_CAP);
//		
//		if(!this.world.isRemote)
//		{
//			if(lazyCaps.isPresent())
//			{
//				IMorphCapability resolvedCaps = lazyCaps.resolve().get();
//				
//				if(!resolvedCaps.getMorphList().contains(getMorphItem()))
//				{
//					resolvedCaps.getMorphList().addToMorphList(getMorphItem());
//					resolvedCaps.syncWithClients(entityIn);
//					this.remove();
//					
//					this.world.playSound(null, getPosition(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.AMBIENT, 2, (this.rand.nextFloat() - 0.5f) + 1);
//				}
//			}
//		}
//	}
 
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
