package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.Tuple;

public class TupleToBoolValueConverter implements TupleToValueConverter<BoolValue, Boolean>
{
	@Override
	public BoolValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		Boolean booleanObj = tuple.getBoolean(colName);
		if (booleanObj == null) return null;

		BoolValue boolValue = new BoolValue();
		boolValue.setValue(booleanObj);
		return boolValue;
	}

	@Override
	public Boolean extractValue(Value value)
	{
		return ((BoolValue) value).getValue();
	}
}
