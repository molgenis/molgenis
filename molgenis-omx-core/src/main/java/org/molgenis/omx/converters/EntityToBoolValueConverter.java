package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToBoolValueConverter implements EntityToValueConverter<BoolValue, Boolean>
{
	@Override
	public BoolValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new BoolValue());
	}

	@Override
	public BoolValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof BoolValue))
		{
			throw new ValueConverterException("value is not a " + BoolValue.class.getSimpleName());
		}

		Boolean booleanObj = entity.getBoolean(attributeName);
		if (booleanObj == null) return null;

		BoolValue boolValue = (BoolValue) value;
		((BoolValue) value).setValue(booleanObj);
		return boolValue;
	}

	@Override
	public Cell<Boolean> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof BoolValue))
		{
			throw new ValueConverterException("value is not a " + BoolValue.class.getSimpleName());
		}
		return new ValueCell<Boolean>(((BoolValue) value).getValue());
	}
}
