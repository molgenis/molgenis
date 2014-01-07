package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.BoolValue;
import org.testng.annotations.Test;

public class TupleToBoolValueConverterTest
{
	@Test
	public void toCell() throws ValueConverterException
	{
		BoolValue value = new BoolValue();
		value.setValue(Boolean.TRUE);
		assertEquals(new EntityToBoolValueConverter().toCell(value).getValue(), Boolean.TRUE);
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		Entity entity = new MapEntity(colName, true);
		BoolValue value = new EntityToBoolValueConverter().fromEntity(entity, colName, null);
		assertEquals(value.getValue(), Boolean.TRUE);
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		BoolValue value = new BoolValue();
		value.setValue(Boolean.FALSE);

		String colName = "col";
		Entity entity = new MapEntity(colName, true);
		new EntityToBoolValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), Boolean.TRUE);
	}
}
