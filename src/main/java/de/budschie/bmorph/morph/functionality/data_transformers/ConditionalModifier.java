package de.budschie.bmorph.morph.functionality.data_transformers;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiPredicate;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Usage: This modifier can change values based on if-statements. Its first
 * parameter is the "operator" JSON field; it is one of five operators: ">=, ==,
 * <=, <, >" <br>
 * <br>
 * The second parameter is "to_compare", which is the number which we shall use
 * for comparison on the right side. This means that if a is the value given to
 * this modifier and b is the to_compare parameter, the if-term would look like
 * this: "if a == b"; b is on the right side where as a is on the left. Note
 * that although these numbers are always read in as a double; please report any
 * rounding errors to me (if they occur). <br>
 * <br>
 * The third parameter is "then", it is also a double and it is the number that
 * shall be returned if the bool-statement is true. Note that this number will
 * be cast to the type that was inputted to this modifier, so, again, please
 * report any rounding errors if they occur so that I know that this code
 * doesn't work. <br>
 * <br>
 * As the fourth parameter(s), we can chose one of these two parameter
 * names:<br>
 * 1. "else_if": This parameter is just a recursive modifier, meaning that it
 * contains the same parameters that this modifier posses. This allows for big
 * chains of "else_if" statements.<br>
 * 2. "else": This parameter is just a double that will be the output of this
 * modifier if the statement described in 1. and 2. is not true. <br><br>
 * As the fifth and last parameter, we have the optional "data_type" paramater.
 * It can have one of the following values: "byte, short, int, long, float, double".
 * This is the data type that the double given in parameter 3 will be converted to.
 * This has several usecases, for example, you can get a double as an input, but output booleans (booleans are internally just bytes having the value 0 and 1).
 * <br>
 * <br>
 * Note that if none of the if-checks pass, the data will be returned as-is.
 **/
public class ConditionalModifier extends DataModifier
{	
	public static enum DataType
	{
		BYTE("byte", Tag.TAG_BYTE),
		SHORT("short", Tag.TAG_SHORT),
		INT("int", Tag.TAG_INT),
		LONG("long", Tag.TAG_LONG),
		FLOAT("float", Tag.TAG_FLOAT),
		DOUBLE("double", Tag.TAG_DOUBLE);
		
		private static LazyOptional<HashMap<String, DataType>> types = LazyOptional.of(() ->
		{
			HashMap<String, DataType> dMap = new HashMap<>();
			
			for(DataType type : DataType.values())
			{
				dMap.put(type.getDataTypeName(), type);
			}
			
			return dMap;
		});
		
		private String dataTypeName;
		private byte dataTypeId;
		
		private DataType(String dataTypeName, byte dataTypeId)
		{
			this.dataTypeName = dataTypeName;
			this.dataTypeId = dataTypeId;
		}
		
		public byte getDataTypeId()
		{
			return dataTypeId;
		}
		
		public String getDataTypeName()
		{
			return dataTypeName;
		}
		
		public static DataType getDataTypeByName(String name)
		{
			return types.resolve().get().get(name);
		}
	}
	
	public static enum OperatorType
	{		
		GEQUALS(">=", (a, b) -> ((NumericTag)a).getAsDouble() >= b), 
		EQUALS("==", (a, b) -> ((NumericTag)a).getAsDouble() > b), 
		LEQUALS("<=", (a, b) -> ((NumericTag)a).getAsDouble() <= b), 
		LESS("<", (a, b) -> ((NumericTag)a).getAsDouble() < b), 
		GREATER(">", (a, b) -> ((NumericTag)a).getAsDouble() > b);
	
		private static LazyOptional<HashMap<String, OperatorType>> operators = LazyOptional.of(() ->
		{
			HashMap<String, OperatorType> opMap = new HashMap<>();
			
			for(OperatorType type : OperatorType.values())
			{
				opMap.put(type.getOperatorName(), type);
			}
			
			return opMap;
		});
		
		private String operatorName;
		private BiPredicate<Tag, Double> operation;
		
		OperatorType(String operatorName, BiPredicate<Tag, Double> operation)
		{
			this.operatorName = operatorName;
			this.operation = operation;
		}
		
		public boolean applyOperation(Tag leftSide, double rightSide)
		{
			return operation.test(leftSide, rightSide);
		}
		
		public String getOperatorName()
		{
			return operatorName;
		}
		
		public static OperatorType getOperatorByName(String operator)
		{
			return operators.resolve().get().get(operator);
		}
	}
	
	private static final Codec<OperatorType> OPERATOR_CODEC = Codec.STRING.flatXmap(convertToOp ->
	{
		OperatorType type = OperatorType.getOperatorByName(convertToOp);
		
		if(type == null)
			return DataResult.error(String.format("Unknown operator type \"%s\" in your conditional_modifier."));
		else
			return DataResult.success(type);
	}, opType -> DataResult.success(opType.getOperatorName()));

	// This copy-pasta can be eliminated, but I'm too lazy honestly.
	private static final Codec<DataType> DATA_TYPE_CODEC = Codec.STRING.flatXmap(convertToOp ->
	{
		DataType type = DataType.getDataTypeByName(convertToOp);
		
		if(type == null)
			return DataResult.error(String.format("Unknown data type \"%s\" in your conditional_modifier."));
		else
			return DataResult.success(type);
	}, opType -> DataResult.success(opType.getDataTypeName()));

	
	public static final Codec<ConditionalModifier> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(OPERATOR_CODEC.fieldOf("operator").forGetter(ConditionalModifier::getOperatorType),
					Codec.DOUBLE.fieldOf("to_compare").forGetter(ConditionalModifier::getToCompare),
					Codec.DOUBLE.fieldOf("then").forGetter(ConditionalModifier::getThen),
					Codec.mapEither(Codec.of(new Encoder<ConditionalModifier>()
					{
						@Override
						public <T> DataResult<T> encode(ConditionalModifier input, DynamicOps<T> ops, T prefix)
						{
							return CODEC.encodeStart(ops, input);
						}
					}, new Decoder<ConditionalModifier>()
					{
						@Override
						public <T> DataResult<Pair<ConditionalModifier, T>> decode(DynamicOps<T> ops, T input)
						{
							return CODEC.decode(ops, input);
						}
					}).fieldOf("else_if"), Codec.DOUBLE.fieldOf("else")).forGetter(ConditionalModifier::getElifTerm),
					DATA_TYPE_CODEC.optionalFieldOf("data_type").forGetter(ConditionalModifier::getDataTypeOutput)
					).apply(instance, ConditionalModifier::new));
		
	private OperatorType operatorType;
	private Either<ConditionalModifier, Double> elifTerm;
	private double toCompare;
	private double then;
	private Optional<DataType> dataTypeOutput;
	
	public ConditionalModifier(OperatorType operatorType, double toCompare, double then, Either<ConditionalModifier, Double> elifTerm, Optional<DataType> dataTypeOutput)
	{
		this.operatorType = operatorType;
		this.elifTerm = elifTerm;
		this.toCompare = toCompare;
		this.then = then;
		
		this.dataTypeOutput = dataTypeOutput;
	}
	
	// It depends whether this can operate on numeric tags or not
	@Override
	public boolean canOperateOn(Optional<Tag> nbtTag)
	{
		return nbtTag.isPresent() && nbtTag.get() instanceof NumericTag;
	}

	@Override
	public Optional<Tag> applyModifier(Optional<Tag> inputTag)
	{
		boolean success = operatorType.applyOperation(inputTag.get(), toCompare);
		
		if(success)
		{
			// AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
			return Optional.of(convertToTag(inputTag.get().getId(), then));
		}
		else
		{
			// Check if we should just return a plain value or if we should actually do something:
			if(elifTerm.right().isPresent())
			{
				// byte toConvertTo = dataTypeOutput.isPresent() ? dataTypeOutput.get().getDataTypeId() : inputTag.getId();
				convertToTag(inputTag.get().getId(), elifTerm.right().get());
			}
			else if (elifTerm.left().isPresent())
			{
				if(elifTerm.left().get().canOperateOn(inputTag))
					return elifTerm.left().get().applyModifier(inputTag);
				else
					throw new IllegalArgumentException(String.format("The provided tag type for the ConditionalModifier is illegal."));
			}
		}
		
		return inputTag;
	}
	
	public OperatorType getOperatorType()
	{
		return operatorType;
	}
	
	public Either<ConditionalModifier, Double> getElifTerm()
	{
		return elifTerm;
	}
	
	public double getToCompare()
	{
		return toCompare;
	}
	
	public double getThen()
	{
		return then;
	}
	
	public Optional<DataType> getDataTypeOutput()
	{
		return dataTypeOutput;
	}
	
	private static Tag convertToTag(byte tag, double originalValue)
	{
		// I need some eye bleach
		switch (tag)
		{
		case Tag.TAG_BYTE:	
			return ByteTag.valueOf((byte)originalValue);
		case Tag.TAG_INT:
			return IntTag.valueOf((int)originalValue);
		case Tag.TAG_DOUBLE:
			return DoubleTag.valueOf(originalValue);
		case Tag.TAG_FLOAT:
			return FloatTag.valueOf((float)originalValue);
		case Tag.TAG_LONG:
			return LongTag.valueOf((long)originalValue);
		case Tag.TAG_SHORT:
			return ShortTag.valueOf((short)originalValue);
		default:
			throw new IllegalArgumentException("Unexpected value: " + originalValue);
		}
	}
}