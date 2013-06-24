package org.molgenis.omx.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToDateValueConverter implements TupleToValueConverter<DateValue, String>
{
	private static final String DATEFORMAT_DATE = "yyyy-MM-dd";

	@Override
	public DateValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		String dateStr = tuple.getString(colName);
		if (dateStr == null) return null;

		SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		DateValue dateValue = new DateValue();
		try
		{
			dateValue.setValue(iso8601DateFormat.parse(dateStr));
		}
		catch (ParseException e)
		{
			throw new ValueConverterException(e);
		}
		return dateValue;
	}

	@Override
	public String extractValue(Value value)
	{
		SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		return iso8601DateFormat.format(((DateValue) value).getValue());
	}
}
