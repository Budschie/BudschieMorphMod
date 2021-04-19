package de.budschie.bmorph.morph;

import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;

public class FallbackMorphManager implements IMorphManager<FallbackMorphItem, Void>
{
	private HashMap<String, SpecialDataHandler> dataHandlers = new HashMap<>();
	
	@Override
	public boolean doesManagerApplyTo(EntityType<?> type)
	{
		return true;
	}
	
	@Override
	public FallbackMorphItem createMorphFromEntity(Entity entity)
	{
		return createMorph(entity.getType(), entity.serializeNBT(), null);
	}

	@Override
	public FallbackMorphItem createMorph(EntityType<?> entity, CompoundNBT nbt, Void data)
	{
		SpecialDataHandler handler = dataHandlers.get(EntityType.getKey(entity).toString());
		
		if(handler != null)
		{
			handler.getDefaultApplier().accept(nbt);
		}
		
		return new FallbackMorphItem(nbt, entity);
	}

	@Override
	public FallbackMorphItem createMorph(EntityType<?> entity, Void data)
	{
		SpecialDataHandler handler = dataHandlers.get(EntityType.getKey(entity).toString());
		
		if(handler == null)
		{
			return new FallbackMorphItem(entity);
		}
		else
		{
			CompoundNBT nbt = new CompoundNBT();
			handler.getDefaultApplier().accept(nbt);
			return new FallbackMorphItem(nbt, entity);
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
			SpecialDataHandler handler = dataHandlers.get(EntityType.getKey(item1.getEntityType()).toString());
			
			return handler == null ? true : handler.getEqualsMethod().test(item1, item2);
		}
	}

	@Override
	public int hashCodeFor(FallbackMorphItem item)
	{
		SpecialDataHandler handler = dataHandlers.get(EntityType.getKey(item.getEntityType()).toString());
		
		if(handler == null)
		{
			return EntityType.getKey(item.getEntityType()).hashCode();
		}
		else
		{
			return handler.getHashFunction().apply(item.getEntityType(), item.serializeAdditional());
		}
	}
	
	public static class SpecialDataHandler
	{
		private BiPredicate<FallbackMorphItem, FallbackMorphItem> equalsMethod;
		private BiFunction<EntityType<?>, CompoundNBT, Integer> hashFunction;
		private Consumer<CompoundNBT> defaultApplier;
		
		/** 
		 * The methods listed below shall be implemented in following manner:
		 * The first thing, the equals method, should simply test of the equality of (counting) contents.
		 * The second argument is the hash function: It should simply combine the hashes of all relevant values.
		 * You should be carefull with the last method, as it can override values.
		 * 
		 **/
		public SpecialDataHandler(BiPredicate<FallbackMorphItem, FallbackMorphItem> equalsMethod,
				BiFunction<EntityType<?>, CompoundNBT, Integer> hashFunction, Consumer<CompoundNBT> defaultApplier)
		{
			this.equalsMethod = equalsMethod;
			this.hashFunction = hashFunction;
			this.defaultApplier = defaultApplier;
		}

		public BiPredicate<FallbackMorphItem, FallbackMorphItem> getEqualsMethod()
		{
			return equalsMethod;
		}

		public BiFunction<EntityType<?>, CompoundNBT, Integer> getHashFunction()
		{
			return hashFunction;
		}

		public Consumer<CompoundNBT> getDefaultApplier()
		{
			return defaultApplier;
		}
	}
}
