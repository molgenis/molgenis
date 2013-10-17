package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.IntValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToIntValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		IntValue value = new IntValue();
		value.setValue(123);
		assertEquals(new TupleToIntValueConverter().toCell(value).getValue(), Integer.valueOf(123));
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		IntValue value = new IntValue();
		value.setValue(123);

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, 123);
		new TupleToIntValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), Integer.valueOf(123));
	}
}
