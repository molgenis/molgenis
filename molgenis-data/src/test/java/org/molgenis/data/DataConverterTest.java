package org.molgenis.data;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
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
	public void convertLocalDate() throws ParseException
	{
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(DATE);
		assertEquals(DataConverter.convert("2015-06-04", attr), LocalDate.parse("2015-06-04"));
	}

	@DataProvider(name = "convertObjectAttributeProvider")
	public static Iterator<Object[]> convertObjectAttributeProvider()
	{
		Object object = mock(Object.class);
		return newArrayList(new Object[] { object, ONE_TO_MANY, object },
				new Object[] { object, XREF, object }).iterator();
	}

	@Test(dataProvider = "convertObjectAttributeProvider")
	public void convertObjectAttribute(Object source, AttributeType attrType, Object convertedValue)
			throws ParseException
	{
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(attrType);
		assertEquals(DataConverter.convert(source, attr), convertedValue);
	}

	@Test
	public void toLocalDate() throws ParseException
	{
		assertEquals(DataConverter.toLocalDate("2015-06-04"), LocalDate.parse("2015-06-04"));
	}

	@Test
	public void convertDateTime() throws ParseException
	{
		Attribute attr = when(mock(Attribute.class).getName()).thenReturn("attr").getMock();
		when(attr.getDataType()).thenReturn(DATE_TIME);
		assertEquals(DataConverter.convert("2015-05-22T11:12:13+0500", attr), Instant.parse("2015-05-22T06:12:13Z"));
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

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Conversion failure in entity type \\[test\\] attribute \\[id\\]; .*")
	public void testWrapExceptionOnInvalidConversion()
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("test");
		Attribute attr = mock(Attribute.class);
		when(attr.getDataType()).thenReturn(AttributeType.INT);
		when(attr.getName()).thenReturn("id");
		when(attr.getEntity()).thenReturn(entityType);

		DataConverter.convert("test", attr);
	}
}
