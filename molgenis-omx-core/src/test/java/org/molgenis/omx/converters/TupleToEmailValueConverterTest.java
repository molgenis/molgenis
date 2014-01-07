package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.EmailValue;
import org.testng.annotations.Test;

public class TupleToEmailValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		EmailValue value = new EmailValue();
		value.setValue("a@b.org");
		assertEquals(new EntityToEmailValueConverter().toCell(value).getValue(), "a@b.org");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		Entity entity = new MapEntity(colName, "a@b.org");
		EmailValue value = new EntityToEmailValueConverter().fromEntity(entity, colName, null);
		assertEquals(value.getValue(), "a@b.org");
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		EmailValue value = new EmailValue();
		value.setValue("a@b.org");

		String colName = "col";
		Entity entity = new MapEntity(colName, "a@b.org");
		new EntityToEmailValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), "a@b.org");
	}
}
