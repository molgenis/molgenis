package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToLongValueConverter implements TupleToValueConverter<LongValue, Long>
{
	@Override
	public LongValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		Long longObj;
		try
		{
			longObj = tuple.getLong(colName);
		}
		catch (RuntimeException e)
		{
			throw new ValueConverterException(e);
		}
		if (longObj == null) return null;

		LongValue longValue = new LongValue();
		longValue.setValue(longObj);
		return longValue;
	}

	@Override
	public Cell<Long> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof LongValue))
		{
			throw new ValueConverterException("value is not a " + LongValue.class.getSimpleName());
		}
		return new ValueCell<Long>(((LongValue) value).getValue());
	}
}
