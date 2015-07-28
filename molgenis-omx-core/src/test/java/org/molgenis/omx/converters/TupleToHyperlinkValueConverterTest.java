package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.HyperlinkValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToHyperlinkValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		HyperlinkValue value = new HyperlinkValue();
		value.setValue("http://www.a.org/");
		assertEquals(new TupleToHyperlinkValueConverter().toCell(value).getValue(), "http://www.a.org/");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		HyperlinkValue value = new HyperlinkValue();
		value.setValue("http://www.a.org/");

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "http://www.a.org/");
		new TupleToHyperlinkValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), "http://www.a.org/");
	}
}
