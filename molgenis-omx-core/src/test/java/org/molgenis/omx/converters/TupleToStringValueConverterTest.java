package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.StringValue;
import org.testng.annotations.Test;

public class TupleToStringValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		StringValue value = new StringValue();
		value.setValue("value");
		assertEquals(new EntityToStringValueConverter().toCell(value).getValue(), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		Entity entity = new MapEntity(colName, "value");
		StringValue value = new EntityToStringValueConverter().fromEntity(entity, colName, null);
		assertEquals(value.getValue(), "value");
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		StringValue value = new StringValue();
		value.setValue("value");

		String colName = "col";
		Entity entity = new MapEntity(colName, "value");
		new EntityToStringValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), "value");
	}
}
