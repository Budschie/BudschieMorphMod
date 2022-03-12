package de.budschie.bmorph.advancements;

import com.google.gson.JsonObject;

import de.budschie.bmorph.advancements.MorphedTrigger.MorphedTriggerInstance;
import de.budschie.bmorph.advancements.predicates.MorphPredicate;
import de.budschie.bmorph.morph.MorphItem;
import de.budschie.bmorph.morph.MorphUtil;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MorphedTrigger extends SimpleCriterionTrigger<MorphedTriggerInstance>
{	
	private ResourceLocation id;
	
	public MorphedTrigger(ResourceLocation id)
	{
		this.id = id;
	}
	
	@Override
	public ResourceLocation getId()
	{
		return id;
	}

	@Override
	protected MorphedTriggerInstance createInstance(JsonObject json, Composite composite, DeserializationContext context)
	{
		return new MorphedTriggerInstance(id, composite, MorphPredicate.fromJson(json));
	}
	
	public void trigger(MorphItem morphItem, ServerPlayer serverPlayer)
	{
		this.trigger(serverPlayer, instance -> instance.matches(morphItem, serverPlayer));
	}
	
	public static class MorphedTriggerInstance extends AbstractCriterionTriggerInstance
	{
		private MorphPredicate morphPredicate;
		
		public MorphedTriggerInstance(ResourceLocation id, Composite composite, MorphPredicate morphPredicate)
		{
			super(id, composite);
			this.morphPredicate = morphPredicate;
		}
		
		@Override
		public JsonObject serializeToJson(SerializationContext context)
		{
			JsonObject object = super.serializeToJson(context);
			object.add("morph", morphPredicate.serializeToJson());
			return object;
		}
		
		public boolean matches(MorphItem morphItem, ServerPlayer serverPlayer)
		{
			return morphPredicate.matches(morphItem, MorphUtil.getCapOrNull(serverPlayer));
		}
	}
}
