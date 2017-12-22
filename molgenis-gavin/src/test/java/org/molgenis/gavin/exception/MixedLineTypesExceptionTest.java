package org.molgenis.gavin.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MixedLineTypesExceptionTest extends ExceptionMessageTest
{

	@BeforeMethod
	public void setUp()
	{
		messageSource.addMolgenisNamespaces("gavin");
	}

	@Test
	public void testGetLocalizedMessageArguments() throws Exception
	{
		assertEquals(new MixedLineTypesException().getLocalizedMessage(),
				"Input file contains mixed line types. Please use one type only, either VCF or CADD.");
	}
}