package org.molgenis.omx.converters;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.omx.observ.value.DateTimeValue;
import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.EmailValue;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.omx.observ.value.MrefValue;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.observ.value.XrefValue;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

/**
 * Converts tuple column values to value entities
 */
public class ValueConverter
{
	private static final Map<Class<? extends Value>, FieldTypeEnum> VALUE_FIELDTYPE_MAP;

	static
	{
		VALUE_FIELDTYPE_MAP = new HashMap<Class<? extends Value>, FieldTypeEnum>();
		VALUE_FIELDTYPE_MAP.put(BoolValue.class, FieldTypeEnum.BOOL);
		VALUE_FIELDTYPE_MAP.put(CategoricalValue.class, FieldTypeEnum.CATEGORICAL);
		VALUE_FIELDTYPE_MAP.put(DateValue.class, FieldTypeEnum.DATE);
		VALUE_FIELDTYPE_MAP.put(DateTimeValue.class, FieldTypeEnum.DATE_TIME);
		VALUE_FIELDTYPE_MAP.put(DecimalValue.class, FieldTypeEnum.DECIMAL);
		VALUE_FIELDTYPE_MAP.put(EmailValue.class, FieldTypeEnum.EMAIL);
		VALUE_FIELDTYPE_MAP.put(HyperlinkValue.class, FieldTypeEnum.HYPERLINK);
		VALUE_FIELDTYPE_MAP.put(IntValue.class, FieldTypeEnum.INT);
		VALUE_FIELDTYPE_MAP.put(LongValue.class, FieldTypeEnum.LONG);
		VALUE_FIELDTYPE_MAP.put(MrefValue.class, FieldTypeEnum.MREF);
		VALUE_FIELDTYPE_MAP.put(StringValue.class, FieldTypeEnum.STRING);
		VALUE_FIELDTYPE_MAP.put(TextValue.class, FieldTypeEnum.TEXT);
		VALUE_FIELDTYPE_MAP.put(XrefValue.class, FieldTypeEnum.XREF);
	}

	private final Database database;
	private final Map<FieldTypeEnum, TupleToValueConverter<? extends Value, ?>> tupleConverters;
	private CharacteristicLoadingCache characteristicLoadingCache;

	public ValueConverter(Database database)
	{
		if (database == null) throw new IllegalArgumentException("Database is null");
		this.database = database;
		this.tupleConverters = new EnumMap<FieldTypeEnum, TupleToValueConverter<? extends Value, ?>>(
				FieldTypeEnum.class);
	}

	public Value fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		FieldType fieldType = MolgenisFieldTypes.getType(feature.getDataType());
		if (fieldType == null)
		{
			throw new ValueConverterException("data type is not a molgenis field type [" + feature.getDataType() + "]");
		}

		TupleToValueConverter<? extends Value, ?> converter = getTupleConverter(fieldType.getEnumType());
		if (converter == null)
		{
			throw new IllegalArgumentException("unsupported field type [" + fieldType.getEnumType() + "]");
		}

		return converter.fromTuple(tuple, colName, feature);
	}

	public Cell<?> toCell(Value value) throws ValueConverterException
	{
		if (value == null) return null;

		FieldTypeEnum fieldTypeEnum = VALUE_FIELDTYPE_MAP.get(value.getClass());
		if (fieldTypeEnum == null)
		{
			throw new ValueConverterException("unknown value type [" + value.getClass().getSimpleName() + "]");
		}
		TupleToValueConverter<? extends Value, ?> valueConverter = getTupleConverter(fieldTypeEnum);
		if (valueConverter == null)
		{
			throw new ValueConverterException("unsupported value type [" + value.getClass().getSimpleName() + "]");
		}
		return valueConverter.toCell(value);
	}

	private TupleToValueConverter<? extends Value, ?> getTupleConverter(FieldTypeEnum fieldTypeEnum)
	{
		TupleToValueConverter<? extends Value, ?> tupleConverter = tupleConverters.get(fieldTypeEnum);
		if (tupleConverter == null)
		{
			// lazy initialization of tuple converters
			switch (fieldTypeEnum)
			{
				case BOOL:
					tupleConverter = new TupleToBoolValueConverter();
					break;
				case CATEGORICAL:
					tupleConverter = new TupleToCategoricalValueConverter(database);
					break;
				case DATE:
					tupleConverter = new TupleToDateValueConverter();
					break;
				case DATE_TIME:
					tupleConverter = new TupleToDateTimeValueConverter();
					break;
				case DECIMAL:
					tupleConverter = new TupleToDecimalValueConverter();
					break;
				case EMAIL:
					tupleConverter = new TupleToEmailValueConverter();
					break;
				case HYPERLINK:
					tupleConverter = new TupleToHyperlinkValueConverter();
					break;
				case INT:
					tupleConverter = new TupleToIntValueConverter();
					break;
				case LONG:
					tupleConverter = new TupleToLongValueConverter();
					break;
				case MREF:
					tupleConverter = new TupleToMrefValueConverter(getCharacteristicLoadingCache());
					break;
				case STRING:
					tupleConverter = new TupleToStringValueConverter();
					break;
				case TEXT:
					tupleConverter = new TupleToTextValueConverter();
					break;
				case XREF:
					tupleConverter = new TupleToXrefValueConverter(getCharacteristicLoadingCache());
					break;
				// $CASES-OMITTED$
				default:
					throw new IllegalArgumentException("unsupported field type [" + fieldTypeEnum + "]");
			}
			tupleConverters.put(fieldTypeEnum, tupleConverter);
		}
		return tupleConverter;
	}

	private CharacteristicLoadingCache getCharacteristicLoadingCache()
	{
		if (characteristicLoadingCache == null)
		{
			characteristicLoadingCache = new CharacteristicLoadingCache(database);
		}
		return characteristicLoadingCache;
	}
}
