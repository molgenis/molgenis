package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToDecimalValueConverter implements TupleToValueConverter<DecimalValue, Double>
{
	@Override
	public DecimalValue fromTuple(Tuple tuple, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		Double doubleObj;
		try
		{
			doubleObj = tuple.getDouble(colName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (doubleObj == null) return null;

		DecimalValue decimalValue = new DecimalValue();
		decimalValue.setValue(doubleObj);
		return decimalValue;
	}

	@Override
	public Double extractValue(Value value)
	{
		return ((DecimalValue) value).getValue();
	}
}
