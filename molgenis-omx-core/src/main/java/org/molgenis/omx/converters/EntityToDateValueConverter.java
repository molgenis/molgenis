package org.molgenis.omx.converters;

import java.text.ParseException;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;
import org.molgenis.util.MolgenisDateFormat;

public class EntityToDateValueConverter implements EntityToValueConverter<DateValue, String>
{
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

		DateValue dateValue = (DateValue) value;
		try
		{
			dateValue.setValue(MolgenisDateFormat.getDateFormat().parse(dateStr));
		}
		catch (ParseException e)
		{
			throw new ValueConverterException(e);
		}
		return dateValue;
	}

	@Override
	public Cell<String> toCell(Value value, ObservableFeature feature) throws ValueConverterException
	{
		if (!(value instanceof DateValue))
		{
			throw new ValueConverterException("value is not a " + DateValue.class.getSimpleName());
		}
		return new ValueCell<String>(MolgenisDateFormat.getDateFormat().format(((DateValue) value).getValue()));
	}
}
