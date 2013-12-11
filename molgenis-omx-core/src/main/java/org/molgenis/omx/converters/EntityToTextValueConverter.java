package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToTextValueConverter implements EntityToValueConverter<TextValue, String>
{
	@Override
	public TextValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new TextValue());
	}

	@Override
	public TextValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof TextValue))
		{
			throw new ValueConverterException("value is not a " + TextValue.class.getSimpleName());
		}
		String text = entity.getString(attributeName);
		if (text == null) return null;

		TextValue textValue = (TextValue) value;
		textValue.setValue(text);
		return textValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof TextValue))
		{
			throw new ValueConverterException("value is not a " + TextValue.class.getSimpleName());
		}
		return new ValueCell<String>(((TextValue) value).getValue());
	}
}
