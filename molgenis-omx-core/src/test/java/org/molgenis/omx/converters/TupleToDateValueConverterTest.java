package org.molgenis.omx.converters;

import static org.testng.Assert.assertEquals;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.observ.value.DateValue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TupleToDateValueConverterTest
{
	private static TimeZone TIMEZONE_CURRENT;

	@BeforeClass
	public void setUpBeforeClass()
	{
		TIMEZONE_CURRENT = DateFormat.getInstance().getTimeZone();
		DateFormat.getInstance().setTimeZone(TimeZone.getTimeZone("GMT+2"));
	}

	@AfterClass
	public void tearDownAfterClass()
	{
		DateFormat.getInstance().setTimeZone(TIMEZONE_CURRENT);
	}

	@Test
	public void toCell() throws ValueConverterException
	{
		Date date = new Date(1371420000000l);
		DateValue value = new DateValue();
		value.setValue(date);
		assertEquals(new EntityToDateValueConverter().toCell(value, null).getValue(), "2013-06-17");
	}

	@Test
	public void fromTuple() throws ValueConverterException
	{
		String colName = "col";
		Entity entity = new MapEntity(colName, "2013-06-17");
		DateValue value = new EntityToDateValueConverter().fromEntity(entity, colName, null);
		assertEquals(value.getValue(), new Date(1371420000000l));
	}

	@Test
	public void updateFromTuple() throws ValueConverterException
	{
		DateValue value = new DateValue();
		String colName = "col";
		Entity entity = new MapEntity(colName, "2013-06-17");
		new EntityToDateValueConverter().updateFromEntity(entity, colName, null, value);
		assertEquals(value.getValue(), new Date(1371420000000l));
	}
}
