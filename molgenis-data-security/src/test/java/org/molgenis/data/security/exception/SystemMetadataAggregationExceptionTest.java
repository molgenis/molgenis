package org.molgenis.data.security.exception;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SystemMetadataAggregationExceptionTest extends ExceptionMessageTest
{
	@Mock
	private EntityType entityType;

	@BeforeMethod
	public void beforeMethod()
	{
		messageSource.addMolgenisNamespaces("data_security");
	}

	@Test
	public void testGetLocalizedMessage() throws Exception
	{
		when(entityType.getLabel("en")).thenReturn("Attribute");
		SystemMetadataAggregationException ex = new SystemMetadataAggregationException(entityType);
		assertEquals(ex.getLocalizedMessage(), "Aggregation on entity 'Attribute' is only allowed for superusers");
	}
}