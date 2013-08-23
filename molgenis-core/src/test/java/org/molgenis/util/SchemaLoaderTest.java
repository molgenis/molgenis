package org.molgenis.util;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

public class SchemaLoaderTest
{

	@Test
	public void getSchema()
	{
		SchemaLoader schemaLoader = new SchemaLoader("example.xsd");
		assertNotNull(schemaLoader.getSchema());
	}
}
