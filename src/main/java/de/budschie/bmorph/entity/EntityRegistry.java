package de.budschie.bmorph.entity;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegistry
{
	public static DeferredRegister<EntityType<?>> ENTITY_REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, References.MODID);
	public static DeferredRegister<DataSerializerEntry> SERIALIZER_REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.DATA_SERIALIZERS, References.MODID);
	
	public static RegistryObject<EntityType<MorphEntity>> MORPH_ENTITY = ENTITY_REGISTRY.register("morph_entity", () ->
	{
		return EntityType.Builder.<MorphEntity>of
				(
					(type, world) -> new MorphEntity(type, world), MobCategory.MISC
				)
				.clientTrackingRange(20)
				.sized(1, 2)
				.build("bmorph:morph_entity");
	});
	
	public static RegistryObject<DataSerializerEntry> MORPH_SERIALIZER = SERIALIZER_REGISTRY.register("morph_serializer", () -> new DataSerializerEntry(new EntityDataSerializer<MorphItem>()
	{
		@Override
		public void write(FriendlyByteBuf buffer, MorphItem item)
		{
			buffer.writeNbt(item.serialize());
		}

		@Override
		public MorphItem read(FriendlyByteBuf buffer)
		{
			return MorphHandler.deserializeMorphItem(buffer.readNbt());
		}

		@Override
		public MorphItem copy(MorphItem item)
		{
			// Nah Im not gonna create a copy constructor here... lol
			return MorphHandler.deserializeMorphItem(item.serialize());
		}
	}));
}
