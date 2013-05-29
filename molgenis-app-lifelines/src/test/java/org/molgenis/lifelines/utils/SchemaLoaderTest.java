package org.molgenis.lifelines.utils;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class SchemaLoaderTest
{

	@Test
	public void getSchema()
	{
		SchemaLoader schemaLoader = new SchemaLoader("EMeasure.xsd");
		assertNotNull(schemaLoader.getSchema());
	}
}
