package org.molgenis.omx.converters;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.HtmlValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToHtmlValueConverter implements TupleToValueConverter<HtmlValue, String>
{
	@Override
	public HtmlValue fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
	{
		String text = tuple.getString(colName);
		if (text == null) return null;

		HtmlValue HtmlValue = new HtmlValue();
		HtmlValue.setValue(text);
		return HtmlValue;
	}

	@Override
	public String extractValue(Value value)
	{
		return ((HtmlValue) value).getValue();
	}
}
