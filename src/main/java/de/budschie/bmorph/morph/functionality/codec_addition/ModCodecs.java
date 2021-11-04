package de.budschie.bmorph.morph.functionality.codec_addition;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import de.budschie.bmorph.morph.LazyTag;
import de.budschie.bmorph.morph.functionality.codec_addition.CommandProvider.Selector;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class ModCodecs
{
	public static final Codec<Attribute> ATTRIBUTE = getRegistryCodec(ForgeRegistries.ATTRIBUTES::getValue);
	public static final Codec<Effect> EFFECTS = getRegistryCodec(ForgeRegistries.POTIONS::getValue);
	public static final Codec<EntityType<?>> ENTITIES = getRegistryCodec(ForgeRegistries.ENTITIES::getValue);
	
	public static final Codec<Selector> SELECTOR_ENUM = getEnumCodec(Selector.class, Selector::values);
	
	public static final Codec<LazyTag<Block>> LAZY_BLOCK_TAGS = getLazyTagCodec(rl -> BlockTags.getCollection().get(rl)); 
	
	public static final Codec<Vector3d> VECTOR_3D = RecordCodecBuilder.create(instance -> instance.group(Codec.DOUBLE.fieldOf("x").forGetter(inst -> inst.x),
			Codec.DOUBLE.fieldOf("y").forGetter(inst -> inst.y), Codec.DOUBLE.fieldOf("z").forGetter(inst -> inst.z)).apply(instance, Vector3d::new));
	
	public static final Codec<EffectInstance> EFFECT_INSTANCE = RecordCodecBuilder.create(instance -> instance
			.group(
					ModCodecs.EFFECTS.fieldOf("potion_effect").forGetter(EffectInstance::getPotion),
					Codec.INT.optionalFieldOf("duration", 40).forGetter(EffectInstance::getDuration),
					Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectInstance::getAmplifier), 
					Codec.BOOL.optionalFieldOf("ambient", false).forGetter(EffectInstance::isAmbient),
					Codec.BOOL.optionalFieldOf("show_particles", false).forGetter(EffectInstance::doesShowParticles), 
					Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(EffectInstance::isShowIcon)
					)
			.apply(instance, EffectInstance::new));
	
	public static final Codec<CommandProvider> COMMAND_PROVIDER = RecordCodecBuilder
			.create(instance -> instance.group(Codec.STRING.fieldOf("command").forGetter(CommandProvider::getCommand), SELECTOR_ENUM.optionalFieldOf("selector", Selector.SELF).forGetter(CommandProvider::getSelector))
					.apply(instance, CommandProvider::new));
	
	public static final <T> Codec<LazyTag<T>> getLazyTagCodec(Function<ResourceLocation, ITag<T>> resolveFunction)
	{
		return ResourceLocation.CODEC.flatXmap((resourceLocation) ->
		{
			if(resourceLocation == null)
			{
				return DataResult.error("The resource location was null; thus there was no tag present.");
			}
			
			return DataResult.success(new LazyTag<>(resourceLocation, resolveFunction));
		}, (fromLazyTag) ->
		{
			return DataResult.success(fromLazyTag.getTagName());
		});
	}
	 
	public static final <A extends Enum<A>> Codec<A> getEnumCodec(Class<A> clazz, Supplier<A[]> values)
	{		
		return new Codec<A>()
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
					System.out.println(String.format("The %s %s is unknown. Here is a list of valid operations: %s", clazz.getName(), str, 
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
	
	public static final Codec<Operation> OPERATION = new Codec<AttributeModifier.Operation>()
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
	public static <A> Codec<A> newCodec(Supplier<A> supplier)
	{
		return new Codec<A>()
		{
			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix)
			{
				return DataResult.<T>success(ops.empty());
			}

			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input)
			{
				return DataResult.success(new Pair<>(supplier.get(), input));
			}
		};
	}
	
	private static <A extends IForgeRegistryEntry<A>> Codec<A> getRegistryCodec(Function<ResourceLocation, A> registryRetrieval)
	{
		return new Codec<A>()
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
