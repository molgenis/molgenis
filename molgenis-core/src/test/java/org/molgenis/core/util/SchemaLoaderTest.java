package org.molgenis.core.util;

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertNotNull;

public class SchemaLoaderTest
{
	@Test
	public void getSchemaFromInputStream() throws IOException
	{
		String schemaStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"></xs:schema>";
		try (InputStream bis = new ByteArrayInputStream(schemaStr.getBytes("UTF-8")))
		{
			SchemaLoader schemaLoader = new SchemaLoader(bis);
			assertNotNull(schemaLoader.getSchema());
		}
	}

	@Test
	public void getSchema()
	{
		SchemaLoader schemaLoader = new SchemaLoader("example.xsd");
		assertNotNull(schemaLoader.getSchema());
	}
}
