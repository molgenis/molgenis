package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToHyperlinkValueConverter implements EntityToValueConverter<HyperlinkValue, String>
{
	@Override
	public HyperlinkValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new HyperlinkValue());
	}

	@Override
	public HyperlinkValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof HyperlinkValue))
		{
			throw new ValueConverterException("value is not a " + HyperlinkValue.class.getSimpleName());
		}
		String hyperlink = entity.getString(attributeName);
		if (hyperlink == null) return null;

		HyperlinkValue hyperlinkValue = (HyperlinkValue) value;
		hyperlinkValue.setValue(hyperlink);
		return hyperlinkValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof HyperlinkValue))
		{
			throw new ValueConverterException("value is not a " + HyperlinkValue.class.getSimpleName());
		}
		return new ValueCell<String>(((HyperlinkValue) value).getValue());
	}
}
