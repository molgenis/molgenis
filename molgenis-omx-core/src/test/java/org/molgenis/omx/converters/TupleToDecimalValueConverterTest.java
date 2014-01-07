package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.DecimalValue;
import org.testng.annotations.Test;

public class TupleToDecimalValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		DecimalValue value = new DecimalValue();
		value.setValue(1.23);
		assertEquals(new EntityToDecimalValueConverter().toCell(value).getValue(), Double.valueOf(1.23));
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		Entity entity = new MapEntity(colName, 1.23);
		;
		DecimalValue value = new EntityToDecimalValueConverter().fromEntity(entity, colName, null);
		assertEquals(value.getValue(), Double.valueOf(1.23));
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		DecimalValue value = new DecimalValue();
		value.setValue(Double.valueOf(1.23));

		String colName = "col";
		Entity entity = new MapEntity(colName, 1.23);
		new EntityToDecimalValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), Double.valueOf(1.23));
	}
}
