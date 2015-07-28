package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToDecimalValueConverter implements TupleToValueConverter<DecimalValue, Double>
{
	@Override
	public DecimalValue fromTuple(Tuple tuple, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new DecimalValue());
	}

	@Override
	public DecimalValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof DecimalValue))
		{
			throw new ValueConverterException("value is not a " + DecimalValue.class.getSimpleName());
		}

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
