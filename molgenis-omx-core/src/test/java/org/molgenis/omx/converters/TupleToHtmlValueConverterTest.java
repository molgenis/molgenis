package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.HtmlValue;
import org.testng.annotations.Test;

public class TupleToHtmlValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		HtmlValue value = new HtmlValue();
		value.setValue("value");
		assertEquals(new EntityToHtmlValueConverter().toCell(value).getValue(), "value");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		Entity entity = new MapEntity(colName, "value");
		HtmlValue value = new EntityToHtmlValueConverter().fromEntity(entity, colName, null);
		assertEquals(value.getValue(), "value");
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		HtmlValue value = new HtmlValue();
		value.setValue("value");

		String colName = "col";
		Entity entity = new MapEntity(colName, "value");
		new EntityToHtmlValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), "value");
	}
}
