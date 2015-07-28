package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.EmailValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToEmailValueConverter implements TupleToValueConverter<EmailValue, String>
{
	@Override
	public EmailValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new EmailValue());
	}

	@Override
	public EmailValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof EmailValue))
		{
			throw new ValueConverterException("value is not a " + EmailValue.class.getSimpleName());
		}
		String email = tuple.getString(colName);
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
