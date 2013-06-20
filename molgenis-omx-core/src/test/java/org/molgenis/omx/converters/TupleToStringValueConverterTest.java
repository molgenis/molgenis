package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.StringValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToStringValueConverterTest
{
	@Test
	public void extractValue() throws ValueConverterException
	{
		StringValue value = new StringValue();
		value.setValue("value");
		assertEquals(new TupleToStringValueConverter().extractValue(value), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		StringValue value = new TupleToStringValueConverter().fromTuple(tuple, colName, null, null);
		assertEquals(value.getValue(), "value");
	}
}
