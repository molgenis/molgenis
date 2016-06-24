package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.util.MolgenisDateFormat;
import org.testng.annotations.Test;

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
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setDataType(MolgenisFieldTypes.DATE);
		assertEquals(DataConverter.convert("2015-06-04", attr), MolgenisDateFormat.getDateFormat().parse("2015-06-04"));
	}

	@Test
	public void convertDateTime() throws ParseException
	{
		AttributeMetaData attr = new DefaultAttributeMetaData("attr").setDataType(MolgenisFieldTypes.DATETIME);
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
		Integer id0 = Integer.valueOf(0);
		Integer id1 = Integer.valueOf(1);
		assertEquals(DataConverter.toIntList(Arrays.asList(id0, id1)), Arrays.asList(id0, id1));
	}

	@Test
	public void toIntListEntity() throws ParseException
	{
		Integer id0 = Integer.valueOf(0);
		Integer id1 = Integer.valueOf(1);
		Entity entity0 = when(mock(Entity.class).getIdValue()).thenReturn(id0).getMock();
		Entity entity1 = when(mock(Entity.class).getIdValue()).thenReturn(id1).getMock();
		assertEquals(DataConverter.toIntList(Arrays.asList(entity0, entity1)), Arrays.asList(id0, id1));
	}
}
