package de.budschie.bmorph.morph.fallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;

import de.budschie.bmorph.events.MorphCreatedFromEntityEvent;
import de.budschie.bmorph.json_integration.JsonMorphNBTHandler;
import de.budschie.bmorph.json_integration.NBTPath;
import de.budschie.bmorph.morph.IMorphManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.nbt.CompoundTag;

public class FallbackMorphManager implements IMorphManager<FallbackMorphItem, Void>
{
	public static final JsonMorphNBTHandler DEFAULT_HANDLER = new JsonMorphNBTHandler(new CompoundTag(), new NBTPath[]{new NBTPath("CustomName")}, new ArrayList<>());
	
	private HashMap<EntityType<?>, IMorphNBTHandler> dataHandlers = new HashMap<>();
	
	@Override
	public boolean doesManagerApplyTo(EntityType<?> type)
	{
		return true;
	}
	
	@Override
	public FallbackMorphItem createMorphFromEntity(Entity entity)
	{
		// return createMorph(entity.getType(), entity.serializeNBT(), null);
		IMorphNBTHandler handler = dataHandlers.get(entity.getType());
		
		CompoundTag tagOut;
		CompoundTag tagIn = entity.serializeNBT();
		if(handler == null)
			tagOut = DEFAULT_HANDLER.applyDefaultNBTData(tagIn);
		else
			tagOut = handler.applyDefaultNBTData(tagIn);
		
		MorphCreatedFromEntityEvent event = new MorphCreatedFromEntityEvent(tagIn, tagOut, entity);
		MinecraftForge.EVENT_BUS.post(event);
		tagOut = event.getTagOut();
		
		return new FallbackMorphItem(tagOut, entity.getType());
	}

	// This code is dumb
	@Override
	public FallbackMorphItem createMorph(EntityType<?> entity, CompoundTag nbt, Void data, boolean forceNBT)
	{
		IMorphNBTHandler handler = dataHandlers.get(entity);
		
		if(!forceNBT)
		{
			if(handler != null)
			{
				nbt = handler.applyDefaultNBTData(nbt);
			}
			else
				nbt = DEFAULT_HANDLER.applyDefaultNBTData(nbt);
		}
		
		return new FallbackMorphItem(nbt, entity);
	}

	@Override
	public FallbackMorphItem createMorph(EntityType<?> entity, Void data)
	{
		IMorphNBTHandler handler = dataHandlers.get(entity);
		
		if(handler == null)
		{
			CompoundTag nbt = new CompoundTag();
			return new FallbackMorphItem(DEFAULT_HANDLER.applyDefaultNBTData(nbt), entity);
		}
		else
		{
			CompoundTag nbt = new CompoundTag();
			return new FallbackMorphItem(handler.applyDefaultNBTData(nbt), entity);
		}
	}

	@Override
	public boolean equalsFor(FallbackMorphItem item1, FallbackMorphItem item2)
	{
		if(item1 == null && item2 == null)
			return true;
		else if(item1 == null || item2 == null)
			return false;
		else if(item1.getEntityType() != item2.getEntityType())
			return false;
		else
		{
			IMorphNBTHandler handler = dataHandlers.get(item1.getEntityType());
			
			return handler == null ? DEFAULT_HANDLER.areEquals(item1, item2) : handler.areEquals(item1, item2);
		}
	}

	@Override
	public int hashCodeFor(FallbackMorphItem item)
	{
		IMorphNBTHandler handler = dataHandlers.get(item.getEntityType());
		
		if(handler == null)
		{
			// return EntityType.getKey(item.getEntityType()).hashCode();
			return DEFAULT_HANDLER.getHashCodeFor(item);
		}
		else
		{
			return handler.getHashCodeFor(item);
		}
	}
	
	public void addDataHandler(EntityType<?> entityType, IMorphNBTHandler dataHandler)
	{
		dataHandlers.put(entityType, dataHandler);
	}
	
	public void setDataHandlers(HashMap<EntityType<?>, IMorphNBTHandler> dataHandlers)
	{
		this.dataHandlers = dataHandlers;
	}
	
	public static class SpecialDataHandler implements IMorphNBTHandler
	{
		private BiPredicate<FallbackMorphItem, FallbackMorphItem> equalsMethod;
		private Function<FallbackMorphItem, Integer> hashFunction;
		private Function<CompoundTag, CompoundTag> defaultApplier;
		
		/** 
		 * The methods listed below shall be implemented in following manner:
		 * The first thing, the equals method, should simply test of the equality of (counting) contents.
		 * The second argument is the hash function: It should simply combine the hashes of all relevant values.
		 * You should be carefull with the last method, as it can override values.
		 * 
		 **/
		public SpecialDataHandler(BiPredicate<FallbackMorphItem, FallbackMorphItem> equalsMethod,
				Function<FallbackMorphItem, Integer> hashFunction, Function<CompoundTag, CompoundTag> defaultApplier)
		{
			this.equalsMethod = equalsMethod;
			this.hashFunction = hashFunction;
			this.defaultApplier = defaultApplier;
		}

		@Override
		public boolean areEquals(FallbackMorphItem item1, FallbackMorphItem item2)
		{
			return equalsMethod.test(item1, item2);
		}

		@Override
		public int getHashCodeFor(FallbackMorphItem item)
		{
			return hashFunction.apply(item);
		}

		@Override
		public CompoundTag applyDefaultNBTData(CompoundTag in)
		{
			return defaultApplier.apply(in);
		}
	}
}
