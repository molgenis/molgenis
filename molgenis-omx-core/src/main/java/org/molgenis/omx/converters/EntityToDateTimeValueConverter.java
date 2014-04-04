package org.molgenis.omx.converters;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DateTimeValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;
import org.molgenis.util.MolgenisDateFormat;

public class EntityToDateTimeValueConverter implements EntityToValueConverter<DateTimeValue, String>
{
	@Override
	public DateTimeValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new DateTimeValue());
	}

	@Override
	public DateTimeValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof DateTimeValue))
		{
			throw new ValueConverterException("value is not a " + DateTimeValue.class.getSimpleName());
		}

		String dateTimeStr = entity.getString(attributeName);
		if (dateTimeStr == null) return null;

		DateTimeValue dateTimeValue = (DateTimeValue) value;
		try
		{
			dateTimeValue.setValue(MolgenisDateFormat.getDateTimeFormat().parse(dateTimeStr));
		}
		catch (ParseException e)
		{
			throw new ValueConverterException(e);
		}
		return dateTimeValue;
	}

	@Override
	public Cell<String> toCell(Value value, ObservableFeature feature) throws ValueConverterException
	{
		if (!(value instanceof DateTimeValue))
		{
			throw new ValueConverterException("value is not a " + DateTimeValue.class.getSimpleName());
		}

		return new ValueCell<String>(MolgenisDateFormat.getDateTimeFormat().format(((DateTimeValue) value).getValue()));
	}
}
