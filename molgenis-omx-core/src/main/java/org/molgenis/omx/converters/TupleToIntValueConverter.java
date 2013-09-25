package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToIntValueConverter implements TupleToValueConverter<IntValue, Integer>
{
	@Override
	public IntValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		Integer integerObj;
		try
		{
			integerObj = tuple.getInt(colName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (integerObj == null) return null;

		IntValue intValue = new IntValue();
		intValue.setValue(integerObj);
		return intValue;
	}

	@Override
	public Cell<Integer> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof IntValue))
		{
			throw new ValueConverterException("value is not a " + IntValue.class.getSimpleName());
		}
		return new ValueCell<Integer>(((IntValue) value).getValue());
	}
}
