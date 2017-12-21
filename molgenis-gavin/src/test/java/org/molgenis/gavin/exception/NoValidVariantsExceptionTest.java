package org.molgenis.gavin.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class NoValidVariantsExceptionTest extends ExceptionMessageTest
{

	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("gavin");
	}

	@Test
	public void testGetLocalizedMessageArguments() throws Exception
	{
		assertEquals(new NoValidVariantsException().getLocalizedMessage(), "Not a single valid variant line found.");
	}
}