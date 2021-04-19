package de.budschie.bmorph.entity;

import de.budschie.bmorph.main.References;
import de.budschie.bmorph.morph.MorphHandler;
import de.budschie.bmorph.morph.MorphItem;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityRegistry
{
	public static DeferredRegister<EntityType<?>> ENTITY_REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, References.MODID);
	public static DeferredRegister<DataSerializerEntry> SERIALIZER_REGISTRY = DeferredRegister.create(ForgeRegistries.DATA_SERIALIZERS, References.MODID);
	
	public static RegistryObject<EntityType<MorphEntity>> MORPH_ENTITY = ENTITY_REGISTRY.register("morph_entity", () ->
	{
		return EntityType.Builder.<MorphEntity>create
				(
					(type, world) -> new MorphEntity(type, world), EntityClassification.MISC
				)
				.trackingRange(20)
				.size(1, 2)
				.build("bmorph:morph_entity");
	});
	
	public static RegistryObject<DataSerializerEntry> MORPH_SERIALIZER = SERIALIZER_REGISTRY.register("morph_serializer", () -> new DataSerializerEntry(new IDataSerializer<MorphItem>()
	{
		@Override
		public void write(PacketBuffer buffer, MorphItem item)
		{
			buffer.writeCompoundTag(item.serialize());
		}

		@Override
		public MorphItem read(PacketBuffer buffer)
		{
			return MorphHandler.deserializeMorphItem(buffer.readCompoundTag());
		}

		@Override
		public MorphItem copyValue(MorphItem item)
		{
			// Nah Im not gonna create a copy constructor here... lol
			return MorphHandler.deserializeMorphItem(item.serialize());
		}
	}));
}
