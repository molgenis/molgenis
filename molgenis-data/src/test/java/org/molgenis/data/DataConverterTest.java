package org.molgenis.data;

import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DATE;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DATE_TIME;
import static org.testng.Assert.assertEquals;

public class DataConverterTest
{
	@Test
	public void convert()
	{
		assertEquals(DataConverter.convert("test", String.class), "test");
		assertEquals(DataConverter.convert(5L, Number.class).longValue(), 5L);
	}

	@Test
	public void convertDate() throws ParseException
	{
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(DATE);
		assertEquals(DataConverter.convert("2015-06-04", attr), MolgenisDateFormat.getDateFormat().parse("2015-06-04"));
	}

	@Test
	public void convertDateTime() throws ParseException
	{
		AttributeMetaData attr = when(mock(AttributeMetaData.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(DATE_TIME);
		assertEquals(DataConverter.convert("2015-05-22T11:12:13+0500", attr),
				MolgenisDateFormat.getDateTimeFormat().parse("2015-05-22T11:12:13+0500"));
	}

	@Test
	public void toListString() throws ParseException
	{
		String id0 = "0";
		String id1 = "1";
		assertEquals(DataConverter.toList(Arrays.asList(id0, id1)), Arrays.asList(id0, id1));
	}

	@Test
	public void toListEntity() throws ParseException
	{
		String id0 = "0";
		String id1 = "1";
		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
		assertEquals(DataConverter.toList(Arrays.asList(entity0, entity1)), Arrays.asList(id0, id1));
	}

	@Test
	public void toIntListInteger() throws ParseException
	{
		Integer id0 = 0;
		Integer id1 = 1;
		assertEquals(DataConverter.toIntList(Arrays.asList(id0, id1)), Arrays.asList(id0, id1));
	}

	@Test
	public void toIntListEntity() throws ParseException
	{
		Integer id0 = 0;
		Integer id1 = 1;
		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
		assertEquals(DataConverter.toIntList(Arrays.asList(entity0, entity1)), Arrays.asList(id0, id1));
	}
}
