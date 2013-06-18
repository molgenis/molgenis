package org.molgenis.omx.converters;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToStringValueConverter implements TupleToValueConverter<StringValue, String>
{
	@Override
	public StringValue fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
	{
		String str = tuple.getString(colName);
		if (str == null) return null;

		StringValue stringValue = new StringValue();
		stringValue.setValue(str);
		return stringValue;
	}

	@Override
	public String extractValue(Value value)
	{
		return ((StringValue) value).getValue();
	}
}
