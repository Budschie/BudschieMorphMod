package de.budschie.bmorph.morph.fallback;

import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphManagerHandlers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class FallbackMorphItem extends MorphItem
{
	private CompoundNBT entityData;
	private EntityType<?> entityType;
	
	public FallbackMorphItem(CompoundNBT entityData, EntityType<?> entityType)
	{
		super("fallback_morph_item");
		this.entityData = entityData;
		this.entityType = entityType;
		entityData.putString("id", entityType.getRegistryName().toString());
	}
	
	public FallbackMorphItem(EntityType<?> entityType)
	{
		this(new CompoundNBT(), entityType);
	}
	
	public FallbackMorphItem()
	{
		super("fallback_morph_item");
	}
	
	public EntityType<?> getEntityType()
	{
		return entityType;
	}
	
	public Entity createEntity(World world)
	{
		return EntityType.loadEntityAndExecute(entityData, world, entity -> entity);
	}
	
	@Override
	public void deserializeAdditional(CompoundNBT nbt)
	{
		this.entityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(nbt.getString("id")));
		this.entityData = nbt;
	}

	@Override
	public CompoundNBT serializeAdditional()
	{
		return entityData;
	}
	
	@Override
	public int hashCode()
	{
		return MorphManagerHandlers.FALLBACK.hashCodeFor(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof FallbackMorphItem)
		{
			FallbackMorphItem casted = (FallbackMorphItem) obj;

			return MorphManagerHandlers.FALLBACK.equalsFor(this, casted);
		}

		return false;
	}
}
