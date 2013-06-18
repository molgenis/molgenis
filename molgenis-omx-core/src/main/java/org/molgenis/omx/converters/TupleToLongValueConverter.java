package org.molgenis.omx.converters;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToLongValueConverter implements TupleToValueConverter<LongValue, Long>
{
	@Override
	public LongValue fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
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
	public Long extractValue(Value value)
	{
		return ((LongValue) value).getValue();
	}
}
