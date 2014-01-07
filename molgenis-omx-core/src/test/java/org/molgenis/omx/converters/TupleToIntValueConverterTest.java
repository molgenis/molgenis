package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.IntValue;
import org.testng.annotations.Test;

public class TupleToIntValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		IntValue value = new IntValue();
		value.setValue(123);
		assertEquals(new EntityToIntValueConverter().toCell(value).getValue(), Integer.valueOf(123));
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		IntValue value = new IntValue();
		value.setValue(123);

		String colName = "col";
		Entity entity = new MapEntity(colName, 123);
		new EntityToIntValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), Integer.valueOf(123));
	}
}
