package de.budschie.bmorph.entity;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.capabilities.MorphCapabilityAttacher;
import de.budschie.bmorph.events.AcquiredMorphEvent;
import de.budschie.bmorph.events.AcquiredMorphEvent.Post;
import de.budschie.bmorph.events.AcquiredMorphEvent.Pre;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.player.PlayerMorphItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;

public class MorphEntity extends Entity
{
	private static final PlayerMorphItem DEFAULT = MorphManagerHandlers.PLAYER.createMorph(EntityType.PLAYER, new GameProfile(new UUID(0, 0), "SteveMcSteve"));
	
	@SuppressWarnings("unchecked")
	private static final EntityDataAccessor<MorphItem> MORPH_ITEM = (EntityDataAccessor<MorphItem>) SynchedEntityData.defineId(MorphEntity.class, EntityRegistry.MORPH_SERIALIZER.get().getSerializer());
	
	private static final EntityDataAccessor<Integer> PICKUP_POSSIBLE_IN = SynchedEntityData.defineId(MorphEntity.class, EntityDataSerializers.INT);
	
	public MorphEntity(EntityType<?> type, Level world)
	{
		super(type, world);
	}
	
	public MorphEntity(Level world, MorphItem morphItem)
	{
		this(world, morphItem, 0);
	}
	
	public MorphEntity(Level world, MorphItem morphItem, int pickupPossibleIn)
	{
		super(EntityRegistry.MORPH_ENTITY.get(), world);
		this.getEntityData().set(MORPH_ITEM, morphItem);
		this.getEntityData().set(PICKUP_POSSIBLE_IN, pickupPossibleIn);
	}
	
	@Override
	public void tick()
	{
		super.tick();
		
		if(this.tickCount > 20 * 300)
			this.remove(RemovalReason.DISCARDED);
		
		int pTime = this.entityData.get(PICKUP_POSSIBLE_IN);
		
		boolean pickupPossible = pTime <= 0;
		
		if(!isOnGround())
			this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y - 0.04f, this.getDeltaMovement().z);
		
        if (!this.onGround || getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) 
        {
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
		
        // Check if the entity is being picked up and handle it
        if(pickupPossible)
        {
			if(!this.level.isClientSide)
			{
				List<Entity> list = this.level.getEntities(this, getBoundingBox());
				
				for(Entity entity : list)
				{
					if(entity instanceof Player player)
					{
						LazyOptional<IMorphCapability> lazyCaps = player.getCapability(MorphCapabilityAttacher.MORPH_CAP);
						
						if(!this.level.isClientSide)
						{
							if(lazyCaps.isPresent())
							{
								IMorphCapability resolvedCaps = lazyCaps.resolve().get();
								
								if(this.entityData.get(MORPH_ITEM).isAllowedToPickUp(player) && !resolvedCaps.getMorphList().contains(getMorphItem()))
								{
									AcquiredMorphEvent.Pre acquiredMorphPre = new Pre(player, resolvedCaps, getMorphItem());
									
									if(!MinecraftForge.EVENT_BUS.post(acquiredMorphPre))
									{
										resolvedCaps.getMorphList().addMorphItem(getMorphItem());
										resolvedCaps.syncMorphAcquisition(getMorphItem());
										this.remove(RemovalReason.DISCARDED);
										
										this.level.playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.AMBIENT, 2, (this.random.nextFloat() - 0.5f) + 1);
										
										AcquiredMorphEvent.Post acquiredMorphEventPost = new Post(player, resolvedCaps, getMorphItem());
										MinecraftForge.EVENT_BUS.post(acquiredMorphEventPost);
									}									
								}
							}
						}
					}
				}
			}
        }
        else
        {
        	this.entityData.set(PICKUP_POSSIBLE_IN, pTime - 1);
        }
	}
	
	// Inherited from mojang item code, as I don't want to reinvent the wheel and keep consistency between item behaviour and morph "item" behaviour
	@Override
	protected void moveTowardsClosestSpace(double x, double y, double z)
	{
		BlockPos blockpos = new BlockPos(x, y, z);
		Vec3 vector3d = new Vec3(x - blockpos.getX(), y - blockpos.getY(),
				z - blockpos.getZ());
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		Direction direction = Direction.UP;
		double d0 = Double.MAX_VALUE;

		for (Direction direction1 : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST,
				Direction.UP })
		{
			blockpos$mutable.setWithOffset(blockpos, direction1);
			if (!this.level.getBlockState(blockpos$mutable).isCollisionShapeFullBlock(this.level, blockpos$mutable))
			{
				double d1 = vector3d.get(direction1.getAxis());
				double d2 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d1 : d1;
				if (d2 < d0)
				{
					d0 = d2;
					direction = direction1;
				}
			}
		}

		float f = this.random.nextFloat() * 0.2F + 0.1F;
		float f1 = direction.getAxisDirection().getStep();
		Vec3 vector3d1 = this.getDeltaMovement().scale(0.75D);
		if (direction.getAxis() == Direction.Axis.X)
		{
			this.setDeltaMovement(f1 * f, vector3d1.y, vector3d1.z);
		} 
		else if (direction.getAxis() == Direction.Axis.Y)
		{
			this.setDeltaMovement(vector3d1.x, f1 * f, vector3d1.z);
		} 
		else if (direction.getAxis() == Direction.Axis.Z)
		{
			this.setDeltaMovement(vector3d1.x, vector3d1.y, f1 * f);
		}

	}
 
	@Override
	protected void defineSynchedData()
	{
		this.entityData.define(MORPH_ITEM, DEFAULT);
		this.entityData.define(PICKUP_POSSIBLE_IN, 0);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt)
	{
		this.entityData.set(MORPH_ITEM, MorphHandler.deserializeMorphItem(nbt.getCompound("MorphData")));
		
		if(nbt.contains("pickup_possible_in"))
			this.entityData.set(PICKUP_POSSIBLE_IN, nbt.getInt("pickup_possible_in"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt)
	{
		nbt.put("MorphData", this.entityData.get(MORPH_ITEM).serialize());
		
		nbt.putInt("pickup_possible_in", this.entityData.get(PICKUP_POSSIBLE_IN));
	}
	
	public MorphItem getMorphItem()
	{
		return this.entityData.get(MORPH_ITEM);
	}

	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}	
	
	@Override
	public boolean isInvisible()
	{
		return false;
	}
}
