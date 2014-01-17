package org.molgenis.omx.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToDateValueConverter implements EntityToValueConverter<DateValue, String>
{
	private static final String DATEFORMAT_DATE = "yyyy-MM-dd";

	@Override
	public DateValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new DateValue());
	}

	@Override
	public DateValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof DateValue))
		{
			throw new ValueConverterException("value is not a " + DateValue.class.getSimpleName());
		}
		String dateStr = entity.getString(attributeName);
		if (dateStr == null) return null;

		SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		DateValue dateValue = (DateValue) value;
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
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof DateValue))
		{
			throw new ValueConverterException("value is not a " + DateValue.class.getSimpleName());
		}
		SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(DATEFORMAT_DATE);
		return new ValueCell<String>(iso8601DateFormat.format(((DateValue) value).getValue()));
	}
}
