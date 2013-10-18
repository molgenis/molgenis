package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToStringValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		StringValue value = new StringValue();
		value.setValue("value");
		assertEquals(new TupleToStringValueConverter().toCell(value).getValue(), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		StringValue value = new TupleToStringValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), "value");
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		StringValue value = new StringValue();
		value.setValue("value");

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		new TupleToStringValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), "value");
	}
}
