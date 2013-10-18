package org.molgenis.omx.converters;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.omx.utils.ValueCell;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.Tuple;

public class TupleToHyperlinkValueConverter implements TupleToValueConverter<HyperlinkValue, String>
{
	@Override
	public HyperlinkValue fromTuple(Tuple tuple, String colName, ObservableFeature feature)
			throws ValueConverterException
	{
		return updateFromTuple(tuple, colName, feature, new HyperlinkValue());
	}

	@Override
	public HyperlinkValue updateFromTuple(Tuple tuple, String colName, ObservableFeature feature, Value value)
			throws ValueConverterException
	{
		if (!(value instanceof HyperlinkValue))
		{
			throw new ValueConverterException("value is not a " + HyperlinkValue.class.getSimpleName());
		}
		String hyperlink = tuple.getString(colName);
		if (hyperlink == null) return null;

		HyperlinkValue hyperlinkValue = (HyperlinkValue) value;
		hyperlinkValue.setValue(hyperlink);
		return hyperlinkValue;
	}

	@Override
	public Cell<String> toCell(Value value) throws ValueConverterException
	{
		if (!(value instanceof HyperlinkValue))
		{
			throw new ValueConverterException("value is not a " + HyperlinkValue.class.getSimpleName());
		}
		return new ValueCell<String>(((HyperlinkValue) value).getValue());
	}
}
