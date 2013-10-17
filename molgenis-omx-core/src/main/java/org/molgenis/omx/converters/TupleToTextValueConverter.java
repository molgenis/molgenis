package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToTextValueConverter implements TupleToValueConverter<TextValue, String>
{
	@Override
	public TextValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new TextValue());
	}

	@Override
	public TextValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof TextValue))
		{
			throw new ValueConverterException("value is not a " + TextValue.class.getSimpleName());
		}
		String text = tuple.getString(colName);
		if (text == null) return null;

		TextValue textValue = (TextValue) value;
		textValue.setValue(text);
		return textValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof TextValue))
		{
			throw new ValueConverterException("value is not a " + TextValue.class.getSimpleName());
		}
		return new ValueCell<String>(((TextValue) value).getValue());
	}
}
