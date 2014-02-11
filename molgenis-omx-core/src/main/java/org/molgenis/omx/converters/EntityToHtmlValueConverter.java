package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.HtmlValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToHtmlValueConverter implements EntityToValueConverter<HtmlValue, String>
{
	@Override
	public HtmlValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new HtmlValue());
	}

	@Override
	public HtmlValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof HtmlValue))
		{
			throw new ValueConverterException("value is not a " + HtmlValue.class.getSimpleName());
		}
		String text = entity.getString(attributeName);
		if (text == null) return null;

		HtmlValue HtmlValue = (HtmlValue) value;
		HtmlValue.setValue(text);
		return HtmlValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof HtmlValue))
		{
			throw new ValueConverterException("value is not a " + HtmlValue.class.getSimpleName());
		}
		return new ValueCell<String>(((HtmlValue) value).getValue());
	}
}
