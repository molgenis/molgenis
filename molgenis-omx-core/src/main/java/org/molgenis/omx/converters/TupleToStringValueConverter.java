package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToStringValueConverter implements TupleToValueConverter<StringValue, String>
{
	@Override
	public StringValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new StringValue());
	}

	@Override
	public StringValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof StringValue))
		{
			throw new ValueConverterException("value is not a " + StringValue.class.getSimpleName());
		}
		String str = tuple.getString(colName);
		if (str == null) return null;

		StringValue stringValue = (StringValue) value;
		stringValue.setValue(str);
		return stringValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof StringValue))
		{
			throw new ValueConverterException("value is not a " + StringValue.class.getSimpleName());
		}
		return new ValueCell<String>(((StringValue) value).getValue());
	}
}
