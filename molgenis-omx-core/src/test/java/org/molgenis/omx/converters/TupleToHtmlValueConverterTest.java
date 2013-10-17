package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.HtmlValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToHtmlValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		HtmlValue value = new HtmlValue();
		value.setValue("value");
		assertEquals(new TupleToHtmlValueConverter().toCell(value).getValue(), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		HtmlValue value = new TupleToHtmlValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), "value");
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		HtmlValue value = new HtmlValue();
		value.setValue("value");

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		new TupleToHtmlValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), "value");
	}
}
