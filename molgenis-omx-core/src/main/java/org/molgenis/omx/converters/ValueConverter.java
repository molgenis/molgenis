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
import org.molgenis.util.tuple.Tuple;

/**
 * Converts tuple column values to value entities
 */
public class ValueConverter
{
	private static final EnumMap<FieldTypeEnum, TupleToValueConverter<? extends Value, ?>> tupleConverters;
	private static final Map<Class<? extends Value>, TupleToValueConverter<? extends Value, ?>> valueConverters;

	static
	{
		tupleConverters = new EnumMap<FieldTypeEnum, TupleToValueConverter<? extends Value, ?>>(FieldTypeEnum.class);
		tupleConverters.put(FieldTypeEnum.BOOL, new TupleToBoolValueConverter());
		tupleConverters.put(FieldTypeEnum.CATEGORICAL, new TupleToCategoricalValueConverter());
		tupleConverters.put(FieldTypeEnum.DATE, new TupleToDateValueConverter());
		tupleConverters.put(FieldTypeEnum.DATE_TIME, new TupleToDateTimeValueConverter());
		tupleConverters.put(FieldTypeEnum.DECIMAL, new TupleToDecimalValueConverter());
		tupleConverters.put(FieldTypeEnum.EMAIL, new TupleToEmailValueConverter());
		tupleConverters.put(FieldTypeEnum.HYPERLINK, new TupleToHyperlinkValueConverter());
		tupleConverters.put(FieldTypeEnum.INT, new TupleToIntValueConverter());
		tupleConverters.put(FieldTypeEnum.LONG, new TupleToLongValueConverter());
		tupleConverters.put(FieldTypeEnum.MREF, new TupleToMrefValueConverter());
		tupleConverters.put(FieldTypeEnum.STRING, new TupleToStringValueConverter());
		tupleConverters.put(FieldTypeEnum.TEXT, new TupleToTextValueConverter());
		tupleConverters.put(FieldTypeEnum.XREF, new TupleToXrefValueConverter());

		valueConverters = new HashMap<Class<? extends Value>, TupleToValueConverter<? extends Value, ?>>();
		valueConverters.put(BoolValue.class, new TupleToBoolValueConverter());
		valueConverters.put(CategoricalValue.class, new TupleToCategoricalValueConverter());
		valueConverters.put(DateValue.class, new TupleToDateValueConverter());
		valueConverters.put(DateTimeValue.class, new TupleToDateTimeValueConverter());
		valueConverters.put(DecimalValue.class, new TupleToDecimalValueConverter());
		valueConverters.put(EmailValue.class, new TupleToEmailValueConverter());
		valueConverters.put(HyperlinkValue.class, new TupleToHyperlinkValueConverter());
		valueConverters.put(IntValue.class, new TupleToIntValueConverter());
		valueConverters.put(LongValue.class, new TupleToLongValueConverter());
		valueConverters.put(MrefValue.class, new TupleToMrefValueConverter());
		valueConverters.put(StringValue.class, new TupleToStringValueConverter());
		valueConverters.put(TextValue.class, new TupleToTextValueConverter());
		valueConverters.put(XrefValue.class, new TupleToXrefValueConverter());
	}

	public static Value fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
	{
		FieldType fieldType = MolgenisFieldTypes.getType(feature.getDataType());
		if (fieldType == null)
		{
			throw new ValueConverterException("data type is not a molgenis field type [" + feature.getDataType() + "]");
		}

		TupleToValueConverter<? extends Value, ?> converter = tupleConverters.get(fieldType.getEnumType());
		if (converter == null)
		{
			throw new IllegalArgumentException("unsupported field type [" + fieldType.getEnumType() + "]");
		}

		return converter.fromTuple(tuple, colName, db, feature);
	}

	public static Object extractValue(Value value) throws ValueConverterException
	{
		if (value == null) return null;
		TupleToValueConverter<? extends Value, ?> valueConverter = valueConverters.get(value.getClass());
		if (valueConverter == null)
		{
			throw new ValueConverterException("unsupported value type [" + value.getClass().getSimpleName() + "]");
		}
		return valueConverter.extractValue(value);
	}
}
