package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.DecimalValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToDecimalValueConverterTest
{
	@Test
	public void extractValue() throws ValueConverterException
	{
		DecimalValue value = new DecimalValue();
		value.setValue(1.23);
		assertEquals(new TupleToDecimalValueConverter().extractValue(value), Double.valueOf(1.23));
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, 1.23);
		DecimalValue value = new TupleToDecimalValueConverter().fromTuple(tuple, colName, null, null);
		assertEquals(value.getValue(), Double.valueOf(1.23));
	}
}
