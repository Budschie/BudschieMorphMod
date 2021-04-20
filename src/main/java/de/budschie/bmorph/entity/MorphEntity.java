package de.budschie.bmorph.entity;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import de.budschie.bmorph.morph.PlayerMorphItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.world.World;
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
	public void tick()
	{
		super.tick();
		//ticksExisted++;
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
