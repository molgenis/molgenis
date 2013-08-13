package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.LongValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToLongValueConverterTest
{
	@Test
	public void toCell()
	{
		LongValue value = new LongValue();
		value.setValue(1234l);
		assertEquals(new TupleToLongValueConverter().toCell(value).getValue(), Long.valueOf(1234l));
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, 1234l);
		LongValue value = new TupleToLongValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), Long.valueOf(1234l));
	}
}
