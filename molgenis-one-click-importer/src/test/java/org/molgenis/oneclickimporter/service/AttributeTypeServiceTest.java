package org.molgenis.oneclickimporter.service;

import org.molgenis.oneclickimporter.service.impl.AttributeTypeServiceImpl;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class AttributeTypeServiceTest
{
	AttributeTypeService attributeTypeService = new AttributeTypeServiceImpl();

	@Test
	public void guessBasicTypes()
	{
		List<Object> columnValues = newArrayList(1, 2, 3);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), INT);

		columnValues = newArrayList("a", "b", "c");
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = newArrayList(true, false, true);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), BOOL);

		columnValues = newArrayList(1.1, 1.2, 1.3);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), DECIMAL);

		columnValues = newArrayList(1L, 2L, 3L);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), LONG);

		columnValues = newArrayList(1L, "abc", 3L);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);
	}

	@Test
	public void guessTypesWithNullValues()
	{
		List<Object> columnValues = newArrayList(null, null, null);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = newArrayList(1, null, null);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), INT);

		columnValues = newArrayList(null, 2, null);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), INT);

		columnValues = newArrayList(null, null, 3);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), INT);
	}

	@Test
	public void guessTypesWithMixedValues()
	{
		List<Object> columnValues = newArrayList(1, "2", null);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = newArrayList(53, "Hallo", 2);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = singletonList(null);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = newArrayList(53,
				"This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. ",
				"This is a short string", "String...", 34.1);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), TEXT);
	}

	@Test
	public void guessEnrichedTypes()
	{
		List<Object> columnValues = newArrayList(
				"This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. This is a very long string. ",
				"This is a short string", "String...");
		assertEquals(attributeTypeService.guessAttributeType(columnValues), TEXT);

		columnValues = newArrayList("2018-01-03T00:00", "2010-05-03T00:00", "2018-02-03T00:00");
		assertEquals(attributeTypeService.guessAttributeType(columnValues), DATE);

		columnValues = newArrayList("2018-01-03T00:00", "2010-05-03T00:00", "2018-02-03T00:00", "Hello World!");
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = newArrayList(1.0d);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), INT);

		columnValues = newArrayList(1.1d);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), DECIMAL);

		columnValues = newArrayList(Integer.MAX_VALUE + 1.5);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), DECIMAL);

		columnValues = newArrayList(Integer.MAX_VALUE + 1.0);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), LONG);

		columnValues = newArrayList(1248723743178143923L);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), LONG);

		columnValues = newArrayList(1);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), INT);

		columnValues = newArrayList(null, null);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), STRING);

		columnValues = newArrayList(123, 1239472398547932875L);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), LONG);

		columnValues = newArrayList(1, 2.2, 3);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), DECIMAL);

		columnValues = newArrayList(123, 54, 1239472398547932875L, 23.0);
		assertEquals(attributeTypeService.guessAttributeType(columnValues), LONG);
	}
}
