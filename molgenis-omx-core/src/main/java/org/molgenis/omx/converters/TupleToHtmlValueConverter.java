package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.HtmlValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToHtmlValueConverter implements TupleToValueConverter<HtmlValue, String>
{
	@Override
	public HtmlValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		String text = tuple.getString(colName);
		if (text == null) return null;

		HtmlValue HtmlValue = new HtmlValue();
		HtmlValue.setValue(text);
		return HtmlValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof HtmlValue))
		{
			throw new ValueConverterException("value is not a " + HtmlValue.class.getSimpleName());
		}
		return new ValueCell<String>(((HtmlValue) value).getValue());
	}
}
