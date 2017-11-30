package org.molgenis.data.security.exception;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class EntityTypePermissionDeniedExceptionTest extends org.molgenis.i18n.test.exception.ExceptionMessageTest
{
	@Mock
	private EntityType entityType;

	@BeforeMethod
	public void beforeMethod()
	{
		messageSource.addMolgenisNamespaces("data_security");
	}

	@Test
	public void testGetLocalizedMessage()
	{
		EntityTypePermissionDeniedException ex = new EntityTypePermissionDeniedException(entityType, Permission.READ);
		when(entityType.getLabel("en")).thenReturn("Countries");
		assertEquals(ex.getLocalizedMessage(), "No 'read' permission on entity type 'Countries'");
	}
}