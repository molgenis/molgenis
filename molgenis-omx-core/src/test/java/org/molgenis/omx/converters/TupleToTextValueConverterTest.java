package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.TextValue;
import org.testng.annotations.Test;

public class TupleToTextValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		TextValue value = new TextValue();
		value.setValue("value");
		assertEquals(new EntityToTextValueConverter().toCell(value).getValue(), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		TextValue value = new TextValue();
		value.setValue("value");

		String colName = "col";
		Entity entity = new MapEntity(colName, "value");
		new EntityToTextValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), "value");
	}
}
