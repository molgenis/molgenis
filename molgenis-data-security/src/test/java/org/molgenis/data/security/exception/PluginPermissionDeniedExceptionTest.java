package org.molgenis.data.security.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.molgenis.security.core.Permission;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PluginPermissionDeniedExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void beforeMethod()
	{
		messageSource.addMolgenisNamespaces("data_security");
	}

	@Test
	public void testGetLocalizedMessage()
	{
		PluginPermissionDeniedException ex = new PluginPermissionDeniedException("home", Permission.READ);
		assertEquals(ex.getLocalizedMessage(), "No 'read' permission on 'home' plugin");
	}
}