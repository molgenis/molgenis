package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToDecimalValueConverter implements EntityToValueConverter<DecimalValue, Double>
{
	@Override
	public DecimalValue fromEntity(Entity entity, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, colName, feature, new DecimalValue());
	}

	@Override
	public DecimalValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof DecimalValue))
		{
			throw new ValueConverterException("value is not a " + DecimalValue.class.getSimpleName());
		}

		Double doubleObj;
		try
		{
			doubleObj = entity.getDouble(attributeName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (doubleObj == null) return null;

		DecimalValue decimalValue = (DecimalValue) value;
		decimalValue.setValue(doubleObj);
		return decimalValue;
	}

	@Override
	public Cell<Double> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof DecimalValue))
		{
			throw new ValueConverterException("value is not a " + DecimalValue.class.getSimpleName());
		}
		return new ValueCell<Double>(((DecimalValue) value).getValue());
	}
}
