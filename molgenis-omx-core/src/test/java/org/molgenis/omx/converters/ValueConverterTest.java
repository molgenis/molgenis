package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueConverterTest
{
	private Database database;

	@BeforeMethod
	public void setUp()
	{
		database = mock(Database.class);
	}

	@Test
	public void toCell() throws ValueConverterException
	{
		BoolValue value = new BoolValue();
		value.setValue(Boolean.TRUE);
		assertEquals(new ValueConverter(database).toCell(value).getValue(), Boolean.TRUE);
	}

	@Test(expectedExceptions = ValueConverterException.class)
	public void extractValue_UnsupportValue() throws ValueConverterException
	{
		new ValueConverter(database).toCell(new Value());
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		ObservableFeature feature = when(mock(ObservableFeature.class).getDataType()).thenReturn("text").getMock();
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		TextValue value = new TextValue();
		value.setValue("value");
		assertEquals(new ValueConverter(database).fromTuple(tuple, colName, feature), value);
	}
}
