package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToIntValueConverter implements EntityToValueConverter<IntValue, Integer>
{
	@Override
	public IntValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new IntValue());
	}

	@Override
	public IntValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof IntValue))
		{
			throw new ValueConverterException("value is not a " + IntValue.class.getSimpleName());
		}
		Integer integerObj;
		try
		{
			integerObj = entity.getInt(attributeName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (integerObj == null) return null;

		IntValue intValue = (IntValue) value;
		intValue.setValue(integerObj);
		return intValue;
	}

	@Override
	public Cell<Integer> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof IntValue))
		{
			throw new ValueConverterException("value is not a " + IntValue.class.getSimpleName());
		}
		return new ValueCell<Integer>(((IntValue) value).getValue());
	}
}
