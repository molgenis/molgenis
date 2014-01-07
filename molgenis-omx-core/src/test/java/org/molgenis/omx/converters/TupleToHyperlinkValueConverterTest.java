package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.HyperlinkValue;
import org.testng.annotations.Test;

public class TupleToHyperlinkValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		HyperlinkValue value = new HyperlinkValue();
		value.setValue("http://www.a.org/");
		assertEquals(new EntityToHyperlinkValueConverter().toCell(value).getValue(), "http://www.a.org/");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		HyperlinkValue value = new HyperlinkValue();
		value.setValue("http://www.a.org/");

		String colName = "col";
		Entity entity = new MapEntity(colName, "http://www.a.org/");
		new EntityToHyperlinkValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), "http://www.a.org/");
	}
}
