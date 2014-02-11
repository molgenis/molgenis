package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToStringValueConverter implements EntityToValueConverter<StringValue, String>
{
	@Override
	public StringValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new StringValue());
	}

	@Override
	public StringValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof StringValue))
		{
			throw new ValueConverterException("value is not a " + StringValue.class.getSimpleName());
		}
		String str = entity.getString(attributeName);
		if (str == null) return null;

		StringValue stringValue = (StringValue) value;
		stringValue.setValue(str);
		return stringValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof StringValue))
		{
			throw new ValueConverterException("value is not a " + StringValue.class.getSimpleName());
		}
		return new ValueCell<String>(((StringValue) value).getValue());
	}
}
