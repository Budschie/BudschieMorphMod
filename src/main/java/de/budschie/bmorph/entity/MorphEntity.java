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
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
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
		
//		this.prevPosX = getPosX();
//		this.prevPosY = getPosY();
//		this.prevPosZ = getPosZ();
		
		if(this.ticksExisted > 20 * 300)
			this.remove();
		
		if(!isOnGround())
			this.setMotion(this.getMotion().x, this.getMotion().y - 0.04f, this.getMotion().z);
//		else if(!world.isRemote)
//			pushOutOfBlocks(this.getPosX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getPosZ());
		
		//this.move(MoverType.SELF, new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()).add(this.getMotion()));
	
		//this.move(MoverType.SELF, new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()));
		
        if (!this.onGround || horizontalMag(this.getMotion()) > (double)1.0E-5F || (this.ticksExisted + this.getEntityId()) % 4 == 0) 
        {
            this.move(MoverType.SELF, this.getMotion());
        }
		
		if(!this.world.isRemote)
		{
			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, getBoundingBox());
			
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
							
							if(this.dataManager.get(MORPH_ITEM).isAllowedToPickUp(player) && !resolvedCaps.getMorphList().contains(getMorphItem()))
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
	
	// Inherited from mojang item code, as I don't want to reinvent the wheel and keep consistency between item behaviour and morph "item" behaviour
	protected void pushOutOfBlocks(double x, double y, double z)
	{
		BlockPos blockpos = new BlockPos(x, y, z);
		Vector3d vector3d = new Vector3d(x - (double) blockpos.getX(), y - (double) blockpos.getY(),
				z - (double) blockpos.getZ());
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		Direction direction = Direction.UP;
		double d0 = Double.MAX_VALUE;

		for (Direction direction1 : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST,
				Direction.UP })
		{
			blockpos$mutable.setAndMove(blockpos, direction1);
			if (!this.world.getBlockState(blockpos$mutable).hasOpaqueCollisionShape(this.world, blockpos$mutable))
			{
				double d1 = vector3d.getCoordinate(direction1.getAxis());
				double d2 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d1 : d1;
				if (d2 < d0)
				{
					d0 = d2;
					direction = direction1;
				}
			}
		}

		float f = this.rand.nextFloat() * 0.2F + 0.1F;
		float f1 = (float) direction.getAxisDirection().getOffset();
		Vector3d vector3d1 = this.getMotion().scale(0.75D);
		if (direction.getAxis() == Direction.Axis.X)
		{
			this.setMotion((double) (f1 * f), vector3d1.y, vector3d1.z);
		} else if (direction.getAxis() == Direction.Axis.Y)
		{
			this.setMotion(vector3d1.x, (double) (f1 * f), vector3d1.z);
		} else if (direction.getAxis() == Direction.Axis.Z)
		{
			this.setMotion(vector3d1.x, vector3d1.y, (double) (f1 * f));
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
