package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.LongValue;
import org.testng.annotations.Test;

public class TupleToLongValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		LongValue value = new LongValue();
		value.setValue(1234l);
		assertEquals(new EntityToLongValueConverter().toCell(value).getValue(), Long.valueOf(1234l));
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		LongValue value = new LongValue();
		value.setValue(1234l);

		String colName = "col";
		Entity entity = new MapEntity(colName, 1234l);
		new EntityToLongValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), Long.valueOf(1234l));
	}
}
