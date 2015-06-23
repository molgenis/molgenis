package org.molgenis.data.mapper.mapping.model;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class CategoryMappingTest
{
	private CategoryMapping<String, String> mapping = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"));

	private CategoryMapping<String, String> mappingWithNullValueInMap = CategoryMapping
			.create("$('LifeLines_GENDER').map({\"0\":null,\"1\":\"0\"}).value();");

	private CategoryMapping<String, String> mappingWithDefault = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"), "3");

	private CategoryMapping<String, String> mappingWithDefaultNull = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"), null);

	private CategoryMapping<String, String> mappingWithNullValue = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"), "3", "5");

	private CategoryMapping<String, String> mappingWithNullValueEqualsNull = CategoryMapping.<String, String> create(
			"blah", ImmutableMap.<String, String> of("Human", "1", "Orc", "2"), "3", null);

	@Test
	public void testCreateFromAlgorithm()
	{
		assertEquals(CategoryMapping.create(mapping.getAlgorithm()), mapping);
		assertEquals(CategoryMapping.create(mappingWithNullValueInMap.getAlgorithm()), mappingWithNullValueInMap);
		assertEquals(CategoryMapping.create(mappingWithDefault.getAlgorithm()), mappingWithDefault);
		assertEquals(CategoryMapping.create(mappingWithDefaultNull.getAlgorithm()), mappingWithDefaultNull);
		assertEquals(CategoryMapping.create(mappingWithNullValue.getAlgorithm()), mappingWithNullValue);
		assertEquals(CategoryMapping.create(mappingWithNullValueEqualsNull.getAlgorithm()),
				mappingWithNullValueEqualsNull);
	}

	@Test
	public void testGetAlgorithm()
	{
		assertEquals(mapping.getAlgorithm(), "$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}).value();");

		assertEquals(mappingWithDefault.getAlgorithm(),
				"$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\").value();");

		assertEquals(mappingWithNullValue.getAlgorithm(),
				"$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\", \"5\").value();");

		assertEquals(mappingWithNullValueEqualsNull.getAlgorithm(),
				"$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\", null).value();");
	}
}
