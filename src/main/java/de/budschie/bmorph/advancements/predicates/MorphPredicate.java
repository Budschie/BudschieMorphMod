package de.budschie.bmorph.advancements.predicates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.budschie.bmorph.capabilities.IMorphCapability;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.advancements.critereon.NbtPredicate;

public class MorphPredicate
{
	// JSON tag: entity_type
	private EntityTypePredicate entityType;
	// JSON tag: morph_count
	private MinMaxBounds.Ints count;
	// JSON tag: nbt
	private NbtPredicate nbtPredicate;
	
	public MorphPredicate(EntityTypePredicate entityType, Ints count, NbtPredicate nbtPredicate)
	{
		this.entityType = entityType;
		this.count = count;
		this.nbtPredicate = nbtPredicate;
	}
	
	public MorphPredicate()
	{
	}
	
	public boolean matches(MorphItem morphItem, IMorphCapability cap)
	{
		if(entityType != null && !entityType.matches(morphItem.getEntityType()))
			return false;
		
		if(count != null && morphItem != null && cap != null && !count.matches(cap.getMorphList().getEntityCount(morphItem.getEntityType())))
			return false;
		
		if(nbtPredicate != null && morphItem != null && !nbtPredicate.matches(morphItem.serializeAdditional()))
			return false;
		
		return true;
	}
	
	public JsonElement serializeToJson()
	{
		JsonObject object = new JsonObject();
		
		if(entityType != null)
			object.add("entity_type", entityType.serializeToJson());
		
		if(count != null)
			object.add("morph_count", count.serializeToJson());

		if(nbtPredicate != null)
			object.add("nbt", nbtPredicate.serializeToJson());
		
		return object;
	}
	
	public static MorphPredicate fromJson(JsonElement jsonElement)
	{
		if(jsonElement == null || !jsonElement.isJsonObject())
			return new MorphPredicate();
		
		EntityTypePredicate entityType = null;
		MinMaxBounds.Ints count = null;
		NbtPredicate nbtPredicate = null;
		
		JsonObject root = (JsonObject) jsonElement;
		
		if(root.has("entity_type"))
			entityType = EntityTypePredicate.fromJson(root.get("entity_type"));
		
		if(root.has("morph_count"))
			count = MinMaxBounds.Ints.fromJson(root.get("morph_count"));
		
		if(root.has("nbt"))
			nbtPredicate = NbtPredicate.fromJson(root.get("nbt"));
		
		return new MorphPredicate(entityType, count, nbtPredicate);
	}
}
