package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.omx.observ.value.DateValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToDateValueConverterTest
{

	@Test
	public void toCell() throws ValueConverterException
	{
		Date date = new Date(1371420000000l);
		DateValue value = new DateValue();
		value.setValue(date);
		assertEquals(new TupleToDateValueConverter().toCell(value).getValue(), "2013-06-17");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "2013-06-17");
		DateValue value = new TupleToDateValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), new Date(1371420000000l));
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		DateValue value = new DateValue();
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "2013-06-17");
		new TupleToDateValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), new Date(1371420000000l));
	}
}
