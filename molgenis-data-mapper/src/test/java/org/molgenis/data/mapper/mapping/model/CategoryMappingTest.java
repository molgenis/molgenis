package org.molgenis.data.mapper.mapping.model;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

public class CategoryMappingTest
{
	private CategoryMapping<String, String> mapping = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"));

	private CategoryMapping<String, String> mappingWithDefault = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"), "3");

	private CategoryMapping<String, String> mappingWithNullValue = CategoryMapping.<String, String> create("blah",
			ImmutableMap.<String, String> of("Human", "1", "Orc", "2"), "3", "5");

	@Test
	public void testCreateFromAlgorithm()
	{
		assertEquals(CategoryMapping.<String, String> create(mapping.getAlgorithm()), mapping);
	}

	@Test
	public void testGetAlgorithm()
	{
		assertEquals(mapping.getAlgorithm(), "$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}).value();");

		assertEquals(mappingWithDefault.getAlgorithm(),
				"$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\").value();");

		assertEquals(mappingWithNullValue.getAlgorithm(),
				"$('blah').map({\"Human\":\"1\",\"Orc\":\"2\"}, \"3\", \"5\").value();");
	}
}
