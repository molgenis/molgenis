package org.molgenis.data.meta;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MetadataIdGeneratorTest
{
	private MetadataIdGenerator metadataIdGenerator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		metadataIdGenerator = new MetadataIdGenerator<String, String>()
		{
			@Override
			public String generateEntityTypeId(String entityIdentity)
			{
				return null;
			}

			@Override
			public String generateAttributeId(String attributeIdentity)
			{
				return null;
			}
		};
	}

	@Test
	public void testGenerateHashcode()
	{
		String id = "0123456789";
		assertEquals(metadataIdGenerator.generateHashcode(id), "c6c784a6");
	}
}