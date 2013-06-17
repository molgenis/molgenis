package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.molgenis.omx.observ.value.DateTimeValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToDateTimeValueConverterTest
{

	@Test
	public void extractValue() throws ValueConverterException
	{
		Date date = new Date(1371447949000l);
		DateTimeValue value = new DateTimeValue();
		value.setValue(date);
		assertEquals(new TupleToDateTimeValueConverter().extractValue(value), "2013-06-17T07:45:49+0200");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "2013-06-17T07:45:49+0200");
		DateTimeValue value = new TupleToDateTimeValueConverter().fromTuple(tuple, colName, null, null);
		assertEquals(value.getValue(), new Date(1371447949000l));
	}
}
