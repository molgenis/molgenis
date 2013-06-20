package org.molgenis.omx.converters;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToIntValueConverter implements TupleToValueConverter<IntValue, Integer>
{
	@Override
	public IntValue fromTuple(Tuple tuple, String colName, Database db, ObservableFeature feature)
			throws ValueConverterException
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
	public Integer extractValue(Value value)
	{
		return ((IntValue) value).getValue();
	}
}
