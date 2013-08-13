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
		String hyperlink = tuple.getString(colName);
		if (hyperlink == null) return null;

		HyperlinkValue hyperlinkValue = new HyperlinkValue();
		hyperlinkValue.setValue(hyperlink);
		return hyperlinkValue;
	}

	@Override
	public Cell<String> toCell(Value value)
	{
		return new ValueCell<String>(((HyperlinkValue) value).getValue());
	}
}
