package de.budschie.bmorph.morph.functionality.codec_addition;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.json_integration.ability_groups.AbilityGroupRegistry.AbilityGroup;
import de.budschie.bmorph.main.BMorphMod;
import de.budschie.bmorph.morph.LazyRegistryWrapper;
import de.budschie.bmorph.morph.LazyTag;
import de.budschie.bmorph.morph.functionality.Ability;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider.Selector;
import de.budschie.bmorph.morph.functionality.data_transformers.DataTransformer;
import de.budschie.bmorph.util.DynamicRegistry;
import de.budschie.bmorph.util.IDynamicRegistryObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.server.ServerLifecycleHooks;

// This class is a fucking mess
/** Contains useful codecs for the abilities **/
public class ModCodecs
{	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final Codec<SoundEvent> SOUND_EVENT_CODEC = Codec.of(new Encoder<SoundEvent>()
	{
		@Override
		public <T> DataResult<T> encode(SoundEvent input, DynamicOps<T> ops, T prefix)
		{
			return DataResult.success(ops.createString(input.getRegistryName().toString()));
		}
	}, new Decoder<SoundEvent>()
	{
		@Override
		public <T> DataResult<Pair<SoundEvent, T>> decode(DynamicOps<T> ops, T input)
		{
			DataResult<String> id = ops.getStringValue(input);
			
			if(id.result().isPresent())
			{
				SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(id.result().get()));
				
				if(soundEvent == null)
				{
					return SoundEvent.CODEC.decode(ops, input);
				}
				else
				{
					return DataResult.success(Pair.of(soundEvent, input));
				}
			}
			else
				return DataResult.error(id.error().get().message());
		}
	});
	
	public static final Codec<Attribute> ATTRIBUTE = getForgeRegistryCodec(ForgeRegistries.ATTRIBUTES::getValue);
	public static final Codec<MobEffect> EFFECTS = getForgeRegistryCodec(ForgeRegistries.MOB_EFFECTS::getValue);
	public static final Codec<EntityType<?>> ENTITIES = getForgeRegistryCodec(ForgeRegistries.ENTITIES::getValue);
	public static final Codec<Block> BLOCKS = getForgeRegistryCodec(ForgeRegistries.BLOCKS::getValue);
	public static final Codec<Fluid> FLUIDS = getForgeRegistryCodec(ForgeRegistries.FLUIDS::getValue);
	public static final Codec<ParticleType<?>> PARTICLE_TYPE = getForgeRegistryCodec(ForgeRegistries.PARTICLE_TYPES::getValue);
	
	public static final Codec<Selector> SELECTOR_ENUM = getEnumCodec(Selector.class, Selector::values);
	public static final Codec<Direction> DIRECTION_ENUM = getEnumCodec(Direction.class, Direction::values);
	
	public static final Codec<BossBarColor> BOSSBAR_COLOR_ENUM = getEnumCodec(BossBarColor.class, BossBarColor::values);
	public static final Codec<BossBarOverlay> BOSSBAR_OVERLAY_ENUM = getEnumCodec(BossBarOverlay.class, BossBarOverlay::values);
	
	public static final Codec<LazyTag<Block>> LAZY_BLOCK_TAGS = getLazyTagCodec(rl -> ForgeRegistries.BLOCKS.tags().getTag(TagKey.create(Registry.BLOCK_REGISTRY, rl)));
	
	public static final Codec<LazyTag<Item>> LAZY_ITEM_TAGS = getLazyTagCodec(rl -> ForgeRegistries.ITEMS.tags().getTag(TagKey.create(Registry.ITEM_REGISTRY, rl)));
	
	public static final Codec<LazyOptional<Ability>> ABILITY = getLazyDynamicRegistry(() -> BMorphMod.DYNAMIC_ABILITY_REGISTRY, "ability", Optional.empty());
	public static final Codec<LazyOptional<AbilityGroup>> ABILITY_GROUP = getLazyDynamicRegistry(() -> BMorphMod.ABILITY_GROUPS, "ability group", Optional.of("#"));
	public static final Codec<LazyOptional<DataTransformer>> DATA_TRANSFORMER = getLazyDynamicRegistry(() -> BMorphMod.DYNAMIC_DATA_TRANSFORMER_REGISTRY, "data transformer", Optional.empty());
	
	public static final Codec<LazyRegistryWrapper<LootItemCondition>> PREDICATE = getLazyMCRegistryObjectCodec(rl -> ServerLifecycleHooks.getCurrentServer().getPredicateManager().get(rl), "predicate");
		
	public static final Codec<net.minecraft.nbt.Tag> NBT_TAG = Codec.STRING.flatXmap(str ->
	{
		net.minecraft.nbt.Tag tag;
		
		try
		{
			tag = new TagParser(new StringReader(str)).readValue();
		}
		catch(CommandSyntaxException ex)
		{
			return DataResult.error("Could not parse NBT data: " + ex.getMessage());
		}
		
		return DataResult.success(tag);
	}, tag -> DataResult.success(tag.toString()));
	
	public static final Codec<net.minecraft.nbt.CompoundTag> NBT_COMPOUND_TAG = Codec.STRING.flatXmap(str ->
	{
		net.minecraft.nbt.CompoundTag tag;
		
		try
		{
			tag = new TagParser(new StringReader(str)).readStruct();
		}
		catch(CommandSyntaxException ex)
		{
			return DataResult.error("Could not parse NBT compound data: " + ex.getMessage());
		}
		
		return DataResult.success(tag);
	}, tag -> DataResult.success(tag.toString()));

	
	
	
	/**
	 * This method creates a codec for a lazy optional for loading in dynamic
	 * registries.
	 * 
	 * @param registry This is the DynamicRegistry object from which the data should
	 *                 be loaded.
	 * @param typeName This shall be the name of the objects that will be
	 *                 registered. These will be displayed in error messages.
	 * @param prefix    The optional prefix that indicates what type this element is.
	 **/
	public static final <R extends IDynamicRegistryObject, D extends DynamicRegistry<R, ?>> Codec<LazyOptional<R>> getLazyDynamicRegistry(Supplier<D> registry, String typeName, Optional<String> idPrefix)
	{
		return new Codec<>()
		{
			@Override
			public <T> DataResult<T> encode(LazyOptional<R> input, DynamicOps<T> ops, T prefix)
			{
				if (input.isPresent())
					return DataResult.success(ops.createString(idPrefix.orElse("") + input.resolve().get().getResourceLocation().toString()));
				else
					return DataResult.error("The given " + typeName + " entry could not be encoded because it is null.");
			}

			@Override
			public <T> DataResult<Pair<LazyOptional<R>, T>> decode(DynamicOps<T> ops, T input)
			{
				DataResult<String> rl = ops.getStringValue(input);

				if (rl.result().isPresent())
				{
					String str = rl.result().get();
					
					if(idPrefix.isPresent())
					{
						if(str.startsWith(idPrefix.get()))
						{
							str = str.substring(idPrefix.get().length());
						}
						else
						{
							return DataResult.error(MessageFormat.format("The given registry entry id {0} did not have {1} as its prefix.", str, idPrefix.get()));
						}
					}
					
					final ResourceLocation result;
					
					try
					{
						result = new ResourceLocation(rl.result().get());
					}
					catch(ResourceLocationException ex)
					{
						return DataResult.error(ex.getMessage());
					}

					return DataResult.success(Pair.of(LazyOptional.of(() -> registry.get().getEntry(result)), input));
				} 
				else
					return DataResult.error(rl.error().get().message());
			}
		};
	};
	
	public static final Codec<Vec3> VECTOR_3D = RecordCodecBuilder.create(instance -> instance.group(Codec.DOUBLE.fieldOf("x").forGetter(inst -> inst.x),
			Codec.DOUBLE.fieldOf("y").forGetter(inst -> inst.y), Codec.DOUBLE.fieldOf("z").forGetter(inst -> inst.z)).apply(instance, Vec3::new));
	
	public static final Codec<Vec3i> VECTOR_3D_I = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("x").forGetter(inst -> inst.getX()),
			Codec.INT.fieldOf("y").forGetter(inst -> inst.getY()), Codec.INT.fieldOf("z").forGetter(inst -> inst.getZ())).apply(instance, Vec3i::new));
	
	public static final Codec<MobEffectInstance> EFFECT_INSTANCE = RecordCodecBuilder.create(instance -> instance
			.group(
					ModCodecs.EFFECTS.fieldOf("potion_effect").forGetter(MobEffectInstance::getEffect),
					Codec.INT.optionalFieldOf("duration", 40).forGetter(MobEffectInstance::getDuration),
					Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier), 
					Codec.BOOL.optionalFieldOf("ambient", false).forGetter(MobEffectInstance::isAmbient),
					Codec.BOOL.optionalFieldOf("show_particles", false).forGetter(MobEffectInstance::isVisible), 
					Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(MobEffectInstance::showIcon)
					)
			.apply(instance, MobEffectInstance::new));
	
	public static final Codec<CommandProvider> COMMAND_PROVIDER = RecordCodecBuilder
			.create(instance -> instance.group(Codec.STRING.fieldOf("command").forGetter(CommandProvider::getCommand), SELECTOR_ENUM.optionalFieldOf("selector", Selector.SELF).forGetter(CommandProvider::getSelector))
					.apply(instance, CommandProvider::new));
	
//	public static final <T> Codec<LazyTag<T>> getLazyTagCodec(Function<ResourceLocation, Tag<T>> resolveFunction)
//	{
//		return ResourceLocation.CODEC.flatXmap((resourceLocation) ->
//		{
//			if(resourceLocation == null)
//			{
//				return DataResult.error("The resource location was null; thus there was no tag present.");
//			}
//			
//			return DataResult.success(new LazyTag<>(resourceLocation, resolveFunction));
//		}, (fromLazyTag) ->
//		{
//			return DataResult.success(fromLazyTag.getTagName());
//		});
//	}
	
	/** Creates a codec that can convert any given resource location to an object and vice versa (all in a lazy manner so that registry entries can be loaded in properly) **/
	public static final <T> Codec<LazyRegistryWrapper<T>> getLazyMCRegistryObjectCodec(Function<ResourceLocation, T> toObject, String name)
	{
		return ResourceLocation.CODEC.flatXmap((resourceLocation) ->
		{
			if(resourceLocation == null)
			{
				return DataResult.error("The resource location was null; thus there was no " + name + " present.");
			}
			
			return DataResult.success(new LazyRegistryWrapper<>(resourceLocation, toObject));
		}, (fromLazyOptional) ->
		{
			return DataResult.success(fromLazyOptional.getResourceLocation());
		});
	}
	
	/** Creates a codec that can convert any given resource location to an object and vice versa (all in a lazy manner so that registry entries can be loaded in properly) **/
	public static final <T> Codec<LazyTag<T>> getLazyTagCodec(Function<ResourceLocation, ITag<T>> toObject)
	{
		return ResourceLocation.CODEC.flatXmap((resourceLocation) ->
		{
			if(resourceLocation == null)
			{
				return DataResult.error("The resource location was null; thus there was no tag present.");
			}
			
			return DataResult.success(new LazyTag<>(resourceLocation, toObject));
		}, (fromLazyOptional) ->
		{
			return DataResult.success(fromLazyOptional.getResourceLocation());
		});
	}

	 
	public static final <A extends Enum<A>> Codec<A> getEnumCodec(Class<A> clazz, Supplier<A[]> values)
	{		
		return new Codec<>()
		{
			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix)
			{
				return DataResult.success(ops.createString(input.name().toLowerCase()));
			}

			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input)
			{
				String str = ops.getStringValue(input).result().get();
				
				boolean hasEnum = true;
				
				A enumParsed = null;
				
				try
				{
					enumParsed = A.valueOf(clazz, str.toUpperCase());
				}
				catch(IllegalArgumentException exception)
				{
					hasEnum = false;
				}
				
				if(hasEnum)
					return DataResult.<Pair<A, T>>success(Pair.of(enumParsed, input));
				else
				{
					LOGGER.warn(String.format("The %s %s is unknown. Here is a list of valid operations: %s", clazz.getName(), str, 
							Stream.of(values.get()).map(validEnum -> validEnum.name().toLowerCase())
								.collect(Collectors.joining(", "))));
					
					return DataResult
							.error(String.format("The %s %s is unknown. Here is a list of valid operations: %s", clazz.getName(), str,
									Stream.of(values.get()).map(validEnum -> validEnum.name().toLowerCase())
											.collect(Collectors.joining(", "))));
				}
			}
		};
	}
	
	public static final Codec<Operation> OPERATION = new Codec<>()
	{
		@Override
		public <T> DataResult<T> encode(Operation input, DynamicOps<T> ops, T prefix)
		{
			return DataResult.success(ops.createString(input.name().toLowerCase()));
		}

		@Override
		public <T> DataResult<Pair<Operation, T>> decode(DynamicOps<T> ops, T input)
		{
			String str = ops.getStringValue(input).result().get();
			
			Operation op = Operation.valueOf(str.toUpperCase());
			
			if(op == null)
				return DataResult
						.error(String.format("The Operation %s is unknown. Here is a list of valid operations: %s", str,
								Stream.of(Operation.values()).map(operation -> operation.name().toLowerCase())
										.collect(Collectors.joining(", "))));
			else
				return DataResult.<Pair<Operation, T>>success(Pair.of(op, input));
		}
	};
	
	// This is dumb...
//	public static <A> Codec<A> newCodec(Supplier<A> supplier)
//	{
//		return new Codec<>()
//		{
//			@Override
//			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix)
//			{
//				return DataResult.<T>success(ops.empty());
//			}
//
//			@Override
//			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input)
//			{
//				return DataResult.success(new Pair<>(supplier.get(), input));
//			}
//		};
//	}

	
	private static <A extends IForgeRegistryEntry<A>> Codec<A> getForgeRegistryCodec(Function<ResourceLocation, A> registryRetrieval)
	{
		return new Codec<>()
		{
			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix)
			{
				return DataResult.<T>success(ops.createString(input.getRegistryName().toString()));
			}

			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input)
			{
				DataResult<String> rl = ops.getStringValue(input);
				
				if(rl.result().isPresent())
				{
					ResourceLocation result = new ResourceLocation(rl.result().get());
					
					A retrieved = registryRetrieval.apply(result);
					
					if(retrieved == null)
						return DataResult.error(String.format("The resource location %s did not yield any registry entry when tried to resolve into an actual instance.", result));
					else
						return DataResult.<Pair<A, T>>success(Pair.of(retrieved, input));
				}
				else
					return DataResult.error(rl.error().get().message());
			}
		};
	}
}
