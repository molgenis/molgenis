package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToBoolValueConverter implements TupleToValueConverter<BoolValue, Boolean>
{
	@Override
	public BoolValue fromTuple(Tuple tuple, String colName, ObservableFeature feature) throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new BoolValue());
	}

	@Override
	public BoolValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof BoolValue))
		{
			throw new ValueConverterException("value is not a " + BoolValue.class.getSimpleName());
		}

		Boolean booleanObj = tuple.getBoolean(colName);
		if (booleanObj == null) return null;

		BoolValue boolValue = (BoolValue) value;
		((BoolValue) value).setValue(booleanObj);
		return boolValue;
	}

	@Override
	public Cell<Boolean> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof BoolValue))
		{
			throw new ValueConverterException("value is not a " + BoolValue.class.getSimpleName());
		}
		return new ValueCell<Boolean>(((BoolValue) value).getValue());
	}
}
