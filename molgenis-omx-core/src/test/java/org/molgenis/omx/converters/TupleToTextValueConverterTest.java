package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToTextValueConverterTest
{
	@Test
	public void extractValue() throws ValueConverterException
	{
		TextValue value = new TextValue();
		value.setValue("value");
		assertEquals(new TupleToTextValueConverter().extractValue(value), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		TextValue value = new TupleToTextValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), "value");
	}
}
