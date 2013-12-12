package org.molgenis.omx.converters;

import org.molgenis.data.Entity;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.EmailValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.Cell;

public class EntityToEmailValueConverter implements EntityToValueConverter<EmailValue, String>
{
	@Override
	public EmailValue fromEntity(Entity entity, String attributeName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromEntity(entity, attributeName, feature, new EmailValue());
	}

	@Override
	public EmailValue updateFromEntity(Entity entity, String attributeName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof EmailValue))
		{
			throw new ValueConverterException("value is not a " + EmailValue.class.getSimpleName());
		}
		String email = entity.getString(attributeName);
		if (email == null) return null;

		EmailValue emailValue = (EmailValue) value;
		emailValue.setValue(email);
		return emailValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof EmailValue))
		{
			throw new ValueConverterException("value is not a " + EmailValue.class.getSimpleName());
		}
		return new ValueCell<String>(((EmailValue) value).getValue());
	}
}
