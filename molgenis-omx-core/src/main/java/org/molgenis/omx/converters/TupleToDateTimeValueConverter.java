package org.molgenis.omx.converters;

import java.text.ParseException;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DateTimeValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.MolgenisDateFormat;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToDateTimeValueConverter implements TupleToValueConverter<DateTimeValue, String>
{
	@Override
	public DateTimeValue fromTuple(Tuple tuple, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new DateTimeValue());
	}

	@Override
	public DateTimeValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof DateTimeValue))
		{
			throw new ValueConverterException("value is not a " + DateTimeValue.class.getSimpleName());
		}

		String dateTimeStr = tuple.getString(colName);
		if (dateTimeStr == null) return null;

		DateTimeValue dateTimeValue = (DateTimeValue) value;
		try
		{
			dateTimeValue.setValue(MolgenisDateFormat.getDateFormat().parse(dateTimeStr));
		}
		catch (ParseException e)
		{
			throw new ValueConverterException(e);
		}
		return dateTimeValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof DateTimeValue))
		{
			throw new ValueConverterException("value is not a " + DateTimeValue.class.getSimpleName());
		}

		return new ValueCell<String>(MolgenisDateFormat.getDateFormat().format(((DateTimeValue) value).getValue()));
	}
}
