package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToLongValueConverter implements EntityToValueConverter<LongValue, Long>
{
	@Override
	public LongValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new LongValue());
	}

	@Override
	public LongValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof LongValue))
		{
			throw new ValueConverterException("value is not a " + LongValue.class.getSimpleName());
		}

		Long longObj;
		try
		{
			longObj = entity.getLong(attributeName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (longObj == null) return null;

		LongValue longValue = (LongValue) value;
		longValue.setValue(longObj);
		return longValue;
	}

	@Override
	public Cell<Long> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof LongValue))
		{
			throw new ValueConverterException("value is not a " + LongValue.class.getSimpleName());
		}
		return new ValueCell<Long>(((LongValue) value).getValue());
	}
}
