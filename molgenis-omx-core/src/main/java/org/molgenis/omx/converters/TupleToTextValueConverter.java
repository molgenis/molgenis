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
		String text = tuple.getString(colName);
		if (text == null) return null;

		TextValue textValue = new TextValue();
		textValue.setValue(text);
		return textValue;
	}

	@Override
	public Cell<String> toCell(Value value)
	{
		return new ValueCell<String>(((TextValue) value).getValue());
	}
}
