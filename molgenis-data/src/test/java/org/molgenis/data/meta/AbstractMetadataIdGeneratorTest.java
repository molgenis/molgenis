package org.molgenis.data.meta;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class AbstractMetadataIdGeneratorTest
{
	private AbstractMetadataIdGenerator abstractMetadataIdGenerator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		abstractMetadataIdGenerator = mock(AbstractMetadataIdGenerator.class, CALLS_REAL_METHODS);
	}

	@Test
	public void testGenerateHashcode()
	{
		String id = "0123456789";
		assertEquals(abstractMetadataIdGenerator.generateHashcode(id), "c6c784a6");
	}
}