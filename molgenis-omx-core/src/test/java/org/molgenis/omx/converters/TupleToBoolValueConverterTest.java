package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToBoolValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		BoolValue value = new BoolValue();
		value.setValue(Boolean.TRUE);
		assertEquals(new TupleToBoolValueConverter().toCell(value).getValue(), Boolean.TRUE);
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, true);
		BoolValue value = new TupleToBoolValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), Boolean.TRUE);
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		BoolValue value = new BoolValue();
		value.setValue(Boolean.FALSE);

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, true);
		new TupleToBoolValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), Boolean.TRUE);
	}
}
