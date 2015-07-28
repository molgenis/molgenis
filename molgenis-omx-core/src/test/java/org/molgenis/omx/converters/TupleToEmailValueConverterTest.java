package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.omx.observ.value.EmailValue;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToEmailValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		EmailValue value = new EmailValue();
		value.setValue("a@b.org");
		assertEquals(new TupleToEmailValueConverter().toCell(value).getValue(), "a@b.org");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "a@b.org");
		EmailValue value = new TupleToEmailValueConverter().fromTuple(tuple, colName, null);
		assertEquals(value.getValue(), "a@b.org");
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		EmailValue value = new EmailValue();
		value.setValue("a@b.org");

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "a@b.org");
		new TupleToEmailValueConverter().updateFromTuple(tuple, colName, null, value);
		assertEquals(value.getValue(), "a@b.org");
	}
}
