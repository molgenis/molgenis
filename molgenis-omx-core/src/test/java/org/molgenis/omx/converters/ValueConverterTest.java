package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataService;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.BoolValue;
import org.molgenis.omx.observ.value.TextValue;
import org.molgenis.omx.observ.value.Value;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValueConverterTest
{
	private DataService dataService;

	@BeforeMethod
	public void setUp()
	{
		dataService = mock(DataService.class);
	}

	@Test
	public void toCell() throws ValueConverterException
	{
		BoolValue value = new BoolValue();
		value.setValue(Boolean.TRUE);
		assertEquals(new ValueConverter(dataService).toCell(value).getValue(), Boolean.TRUE);
	}

	@Test(expectedExceptions = ValueConverterException.class)
	public void extractValue_UnsupportValue() throws ValueConverterException
	{
		new ValueConverter(dataService).toCell(new Value());
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
		assertEquals(new ValueConverter(dataService).fromTuple(tuple, colName, feature), value);
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		ObservableFeature feature = when(mock(ObservableFeature.class).getDataType()).thenReturn("text").getMock();
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, "value");
		TextValue value = new TextValue();
		new ValueConverter(dataService).updateFromTuple(tuple, colName, feature, value);
		assertEquals(value.getValue(), "value");
	}
}
