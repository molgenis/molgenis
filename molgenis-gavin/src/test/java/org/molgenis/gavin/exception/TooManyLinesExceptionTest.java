package org.molgenis.gavin.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TooManyLinesExceptionTest extends ExceptionMessageTest
{
	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("gavin");
	}

	@Test
	public void testGetLocalizedMessageArguments()
	{
		assertEquals(new TooManyLinesException(345).getLocalizedMessage(),
				"Input file contains too many lines. Maximum is 345.");
	}
}